pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    environment {
        // BuildKit reuses cached layers across builds instead of re-writing
        // unchanged ones — meaningfully less disk churn per run on a small EBS volume.
        DOCKER_BUILDKIT = '1'
    }

    stages {

        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm

                // cleanWs() wipes .env every run since it's untracked (and must
                // never be committed — it holds Snowflake/AWS secrets). Restore
                // it from a fixed path outside the workspace that survives builds.
                sh 'cp /opt/invoice-platform/.env .env'
            }
        }

        stage('Check disk space') {
            steps {
                script {
                    // Fail fast with a clear message instead of dying mid-`npm ci`
                    // with an opaque "no space left on device" partway through a build.
                    def availKb = sh(
                        script: "df -Pk / | tail -1 | awk '{print \$4}'",
                        returnStdout: true
                    ).trim().toInteger()
                    def availMb = availKb / 1024

                    echo "Available disk space on /: ${availMb} MB"

                    if (availMb < 2048) {
                        error "Only ${availMb} MB free on / — need at least 2048 MB headroom to safely build images. Run 'docker system prune -af --volumes' or grow the EBS volume before retrying."
                    }
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changed = sh(
                        script: "git diff --name-only HEAD~1 HEAD || true",
                        returnStdout: true
                    ).trim().split('\n')

                    env.BACKEND = changed.any { it.startsWith('backend/') } ? "true" : "false"
                    env.FRONTEND = changed.any { it.startsWith('frontend/') } ? "true" : "false"

                    echo "BACKEND=${env.BACKEND}"
                    echo "FRONTEND=${env.FRONTEND}"
                }
            }
        }

        stage('Deploy (Docker Compose)') {
            steps {
                script {
                    // Reclaim space from any previous run's dangling layers *before*
                    // building — this is what actually prevents the mid-build
                    // "no space left on device" failure, not just post-run cleanup.
                    sh 'docker system prune -af --volumes || true'

                    def services = []
                    if (env.BACKEND == "true")  { services << 'backend' }
                    if (env.FRONTEND == "true") { services << 'frontend' }

                    if (services) {
                        // nginx has no heavy deps to install, but rebuild it whenever
                        // an upstream service changes so its image stays in sync.
                        services << 'nginx'
                        sh "docker compose build ${services.join(' ')}"
                    } else {
                        echo 'No backend/frontend changes — reusing existing images.'
                    }

                    sh 'docker compose up -d --remove-orphans'
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh 'docker system prune -af || true'
        }
    }
}