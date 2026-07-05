// Jenkinsfile — monorepo CI for financial-invoice-analytics-platform
// pipeline is deliberately sequential, not parallel — parallel Maven + npm builds
// keeps each build fast by only touching the module that actually changed.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout(false)
    }

    environment {
        MAVEN_OPTS = '-Xmx512m'
        NODE_OPTIONS = '--max-old-space-size=512'
        DOCKERHUB_REPO = 'eieimk/financial-invoice-analytics-platform'
    }

    stages {
        stage('Detect changed paths') {
            steps {
                script {
                    def baseRef = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: sh(
                        script: 'git rev-parse HEAD~1 2>/dev/null || git rev-parse HEAD',
                        returnStdout: true
                    ).trim()

                    def changedFiles = sh(
                        script: "git diff --name-only ${baseRef} HEAD || true",
                        returnStdout: true
                    ).trim()

                    echo "Base ref: ${baseRef}"
                    echo "Changed files:\n${changedFiles}"

                    env.BUILD_BACKEND = changedFiles.split('\n').any { it.startsWith('backend/') } ? 'true' : 'false'
                    env.BUILD_FRONTEND = changedFiles.split('\n').any { it.startsWith('frontend/') } ? 'true' : 'false'

                    if (changedFiles == '') {
                        env.BUILD_BACKEND = 'true'
                        env.BUILD_FRONTEND = 'true'
                    }

                    echo "BUILD_BACKEND=${env.BUILD_BACKEND}  BUILD_FRONTEND=${env.BUILD_FRONTEND}"
                }
            }
        }

        stage('Backend: build & test') {
            when { environment name: 'BUILD_BACKEND', value: 'true' }
            steps {
                dir('backend') {
                    sh 'mvn -B -T 1 -DskipITs=false clean verify'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Frontend: build & test') {
            when { environment name: 'BUILD_FRONTEND', value: 'true' }
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run lint'
                    sh 'npm run test -- --run'
                    sh 'npm run build'
                }
            }
        }

        stage('Snowflake: lint SQL') {
            when { expression { false } } // Disabled
            steps {
                echo 'Snowflake DDL/scripts changed — no automated schema deploy configured; review manually.'
            }
        }

        stage('Docker build (main only)') {
            when {
                allOf {
                    branch 'main'
                    anyOf {
                        environment name: 'BUILD_BACKEND', value: 'true'
                        environment name: 'BUILD_FRONTEND', value: 'true'
                    }
                }
            }
            steps {
                script {
                    if (env.BUILD_BACKEND == 'true') {
                        sh "docker build -t ${DOCKERHUB_REPO}-backend:${env.GIT_COMMIT.take(7)} backend"
                    }
                    if (env.BUILD_FRONTEND == 'true') {
                        sh "docker build -t ${DOCKERHUB_REPO}-frontend:${env.GIT_COMMIT.take(7)} frontend"
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
