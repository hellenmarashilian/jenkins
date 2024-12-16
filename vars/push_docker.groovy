def call(Map input_values) {
    def default_values = [
        GIT_REPO_URL: 'https://github.com/hellenmarashilian/docker.git',
        GIT_BRANCH: 'main',
        DOCKER_REGISTRY: '192.168.1.157:8083/repository/hellz-docker',
        IMAGE_NAME: 'http_get',
        IMAGE_TAG: 'latest',
        NEXUS_USERNAME: 'admin',
        NEXUS_PASSWORD: '2188'
    ]
    def map_to_apply = default_values + input_values
    pipeline {
        agent any
        stages {
            stage('Checkout Code') {
                steps {
                    script {
                        echo 'Cloning repository...'
                        git branch: map_to_apply.GIT_BRANCH, url: map_to_apply.GIT_REPO_URL
                    }
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        echo 'Building Docker image...'
                        bat "docker build -t ${map_to_apply.IMAGE_NAME}:${map_to_apply.IMAGE_TAG} ./${map_to_apply.IMAGE_NAME}"
                        }
                    }
                }
            stage('Docker Scout SAST Scan') {
                steps {
                    script {
                        echo 'Running SAST Scan with Docker Scout...'
                        withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            echo "Username: $DOCKER_USERNAME"
                            echo "Password length: ${DOCKER_PASSWORD.length()}"
                        }
                        echo %USERPROFILE%
                        withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            bat """
                                echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                                docker scout cves local://${map_to_apply.IMAGE_NAME}:${map_to_apply.IMAGE_TAG} --exit-code --only-severity critical
                            """
                        }
                    }
                }
            }

            stage('Push to Nexus Repository') {
                steps {
                    script {
                        echo 'Pushing Docker image to Nexus...'
                        bat """
                            docker login ${map_to_apply.DOCKER_REGISTRY} -u ${map_to_apply.NEXUS_USERNAME} -p ${map_to_apply.NEXUS_PASSWORD}
                            docker tag ${map_to_apply.IMAGE_NAME}:${map_to_apply.IMAGE_TAG} ${map_to_apply.DOCKER_REGISTRY}/${map_to_apply.IMAGE_NAME}:${map_to_apply.IMAGE_TAG}
                            docker push ${map_to_apply.DOCKER_REGISTRY}/${map_to_apply.IMAGE_NAME}:${map_to_apply.IMAGE_TAG}
                        """
                    }
                }
            }
        }
    }
}
