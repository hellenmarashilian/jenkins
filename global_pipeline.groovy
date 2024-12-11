 pipeline {
        agent any
        stages {
            stage('testing') {
                steps {
                    script {
                        echo 'testing'
                      
                    }
                }
            }
        }
    }
