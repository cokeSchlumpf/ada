pipeline {
    agent { docker { image 'gradle:4.6-jdk8' } }
    stages {
        stage('build') {
            steps {
                sh 'gradle --version'
            }
        }
    }
}
