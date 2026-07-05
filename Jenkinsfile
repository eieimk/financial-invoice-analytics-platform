pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    environment {
        // FIX: redirect ALL temp usage
        TMPDIR = '/var/lib/jenkins/tmp'

        // FIX: Maven cache + Java temp
        MAVEN_OPTS = '''
            -Dmaven.repo.local=/var/lib/jenkins/.m2
            -Djava.io.tmpdir=/var/lib/jenkins/tmp
        '''

        // FIX: Node cache
        NPM_CONFIG_CACHE = '/var/lib/jenkins/.npm'
    }

    stages {

        stage('Prepare Workspace') {
            steps {
                sh '''
                    mkdir -p /var/lib/jenkins/tmp
                    mkdir -p /var/lib/jenkins/.m2
                    mkdir -p /var/lib/jenkins/.npm
                '''
                cleanWs()
                checkout scm
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

                    echo "Backend = ${env.BACKEND}"
                    echo "Frontend = ${env.FRONTEND}"
                }
            }
        }

        stage('Backend Build') {
            when { expression { env.BACKEND == "true" } }
            steps {
                dir('backend') {
                    sh '''
                        mvn -B clean package -DskipTests
                    '''
                }
            }
        }

        stage('Frontend Build') {
            when { expression { env.FRONTEND == "true" } }
            steps {
                dir('frontend') {
                    sh '''
                        npm ci --cache /var/lib/jenkins/.npm
                        npm run build
                    '''
                }
            }
        }

        stage('Docker Build & Deploy') {
            when {
                anyOf {
                    expression { env.BACKEND == "true" }
                    expression { env.FRONTEND == "true" }
                }
            }
            steps {
                script {

                    if (env.BACKEND == "true") {
                        sh '''
                            docker build -t backend:latest backend
                            docker compose up -d backend
                        '''
                    }

                    if (env.FRONTEND == "true") {
                        sh '''
                            docker build -t frontend:latest frontend
                            docker compose up -d frontend
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                try {
                    cleanWs(cleanWhenNotBuilt: false)
                } catch (e) {
                    echo "cleanWs skipped"
                }

                // FIX: reduce disk pressure
                sh '''
                    docker system prune -af || true
                    rm -rf /tmp/* || true
                '''
            }
        }
    }
}