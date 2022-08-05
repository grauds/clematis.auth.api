pipeline {

    agent any

    stages {

        stage("Verify tooling") {
            steps {
                sh '''
              cd jenkins
              docker version
              docker info
              docker compose version
              curl --version
              jq --version
              docker compose ps
            '''
            }
        }

        stage('Get code') {
            steps {
                // Get some code from a GitHub repository
                git branch: 'main', url: 'https://github.com/grauds/clematis.auth.api.git'
                sh 'chmod +x gradlew'
            }
        }

        stage('Gradle build') {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('Build docker image') {
            steps {
                sh 'docker build -t clematis.auth.api .'
            }
        }

        stage("Stop and remove old infrastructure, volumes and containers") {
            steps {
                sh '''
                cd jenkins
                docker compose ps
                docker compose down -v 
                '''
            }
        }

        stage("Build and start docker compose services") {
            environment {
                SPRING_DATASOURCE_PASSWORD = credentials('MYSQL_SPRING_DATASOURCE_PASSWORD')
            }
            steps {
                sh '''
                 cd jenkins
                 docker compose build --build-arg SPRING_DATASOURCE_PASSWORD='$MYSQL_SPRING_DATASOURCE_PASSWORD'
                 docker compose up -d 
              '''
            }
        }
    }

    post {
        always {
            junit '**/build/**/test-results/test/*.xml'
        }
    }
}
