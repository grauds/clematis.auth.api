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

        stage("Build and start docker compose service") {
            environment {
                SPRING_DATASOURCE_PASSWORD = credentials('KEYCLOAK_DB_PASSWORD')
            }
            steps {
                sh '''
                cd jenkins
                docker compose stop
                docker stop clematis-auth-api || true && docker rm clematis-auth-api || true
                docker stop clematis-auth-mysql-db || true && docker rm clematis-auth-mysql-db || true
                docker compose build --build-arg SPRING_DATASOURCE_PASSWORD='$SPRING_DATASOURCE_PASSWORD'
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
