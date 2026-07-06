pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    stages {

        stage('Checkout') {
            steps {
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

                    echo "BACKEND=${env.BACKEND}"
                    echo "FRONTEND=${env.FRONTEND}"
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {

                    if (env.BACKEND == "true") {
                        sh 'docker build -t backend:latest backend'
                    }

                    if (env.FRONTEND == "true") {
                        sh 'docker build -t frontend:latest frontend'
                    }
                }
            }
        }

        stage('Deploy (Local Docker Compose)') {
            steps {
                sh '''
                    docker compose down || true
                    docker compose up -d --build
                '''
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