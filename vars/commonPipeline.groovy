#!/usr/bin/env groovy
def call(sharedLibrary, svcName, buildCommands, pod, slackChannel) {
    def podLabel = "${svcName}-${UUID.randomUUID().toString().substring(0,8)}"
    def tag

    pipeline {
        agent {
            kubernetes {
                label podLabel
                defaultContainer 'jnlp'
                yaml pod
            }
        }
        stages {
            stage('Initialization') {
                when { expression { buildCommands['compileData'].run } }
                steps {
                    script {
                        echo "Starting Initialization stage"
                        try {
                            sharedLibrary.executeStage("initializaion", buildCommands['compileData'])
                        }  catch(Exception e) {
                            echo "Failed in initializaion stage"
                            echo "${e}"
                            throw e
                        }
                    }
                }
            }
            stage('Compilation') {
                when { expression { buildCommands['compileData'].run } }
                steps {
                    echo "Starting Compilation stage"
                    script {
                        try {
                            sharedLibrary.executeStage("compile", buildCommands['compileData'])
                        }  catch(Exception e) {
                            echo "Failed in compilation stage: ${e.toString()}"
                            throw e
                        }
                    }
                }
            }
            stage('Unit test') {
                when { expression { buildCommands['testData'].run } }
                steps {
                    echo "Starting Unit test stage"
                    script {
                        try {
                            sharedLibrary.executeStage("test", buildCommands['testData'])
                        }  catch(Exception e) {
                            echo "Failed in unit test stage: ${e.toString()}"
                            throw e
                        }
                    }
                }
            }
            stage('Build and Upload Artifact') {
                when { expression { buildCommands['artifactData'].run } }
                steps {
                    echo "Starting Build and Upload Artifact stage"
                    script {
                        try {
                            sharedLibrary.executeStage("artifact", buildCommands['artifactData'])
                        }  catch(Exception e) {
                            echo "Failed in artifact stage: ${e.toString()}"
                            throw e
                        }
                    }
                }
            }
            stage('Integration Tests') {
                when { expression { buildCommands['intTestData'].run } }
                steps {
                    echo "Starting Integration Tests stage"
                    script {
                        try {
                            sharedLibrary.executeStage("int-test", buildCommands['intTestData'])
                        }  catch(Exception e) {
                            echo "Failed in integaration tests stage: ${e.toString()}"
                            throw e
                        }
                    }
                }
            }
            stage('Deployment') {
                when {
                    allOf {
                        expression { buildCommands['deploymentData'].run }
                        anyOf {
                            branch 'master'
                            branch 'main'
                        }
                    }
                }
                steps {
                    script {
                        echo "Starting Deployment stage"
                        try {
                            sharedLibrary.executeStage("deployment", buildCommands['deploymentData'])
                        }  catch(Exception e) {
                            echo "Failed in integaration tests stage: ${e.toString()}"
                            throw e
                        }
                    }
                }
            }
        }
        post {
            always {
                script {
                    if (sharedLibrary.getMetaClass().respondsTo(sharedLibrary, "alwaysStep")) {
                        sharedLibrary.alwaysStep()
                    }
                }
            }
            failure {
                script {
                    if (sharedLibrary.getMetaClass().respondsTo(sharedLibrary, "failureStep")) {
                        sharedLibrary.failureStep()
                    } else { // Default failure is to set the tag as description
                        utils.setDescription(tag)
                    }
                }
            }
            success {
                script {
                    if (sharedLibrary.getMetaClass().respondsTo(sharedLibrary, "successStep")) {
                        sharedLibrary.successStep()
                    } else { // Default success is to set the tag as description
                        utils.setDescription(tag)
                    }
                }
            }
            unstable {
                script {
                    if (sharedLibrary.getMetaClass().respondsTo(sharedLibrary, "unstableStep")) {
                        sharedLibrary.unstableStep()
                    } else { // Default unstable is to set the tag as description
                        utils.setDescription(tag)
                    }
                }
            }
        }
    }
}
