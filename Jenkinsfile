pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    environment {
        TMPDIR = '/var/lib/jenkins/tmp'
        MAVEN_OPTS = '-Dmaven.repo.local=/var/lib/jenkins/.m2'
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

                    echo "Backend changed: ${env.BACKEND}"
                    echo "Frontend changed: ${env.FRONTEND}"
                }
            }
        }

        stage('Backend Build') {
            when { expression { env.BACKEND == "true" } }
            steps {
                dir('backend') {
                    sh 'mvn -B clean package -DskipTests'
                }
            }
        }

        stage('Frontend Build') {
            when { expression { env.FRONTEND == "true" } }
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
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
                    echo "cleanup skipped"
                }

                sh 'docker system prune -af || true'
            }
        }
    }
}