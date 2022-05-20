#!/usr/bin/env groovy

// Define a shared library
@Field def ml = new org.foo.pipelines.machineLearning()
@Field def dockerFunc = new org.foo.functions.docker()
@Field def k8s = new org.foo.functions.k8s()

// Define a pod
Field def pod = libraryResource 'org/foo/infraTemplate.yaml'
def template_vars = [
    'build_label': params.svcName
]
pod = renderTemplate(pod, template_vars)

// Define variables
@Field def podLabel = "${params.svcName}-${UUID.randomUUID().toString().substring(0,8)}"
@Field def containerName="helm3", helmPath
@Field def githubCred = "naturalett"

// Load manual job properties
new org.foo.properties.machineLearning.machineLearningProperties().machineLearningExtendedParameters()

pipeline {
    agent {
        kubernetes {
            label podLabel
            defaultContainer 'jnlp'
            yaml pod
        }
    }
    stages {
        stage("Initialization") {
            steps {
                script {
                    git credentialsId: githubCred, url: 'https://github.com/naturalett/' + params.svcName, branch: params.commitHash
                }
            }
        }
        stage("versioning") {
            steps {
                script {
                    def image = dockerFunc.pull(
                        imageName: params.svcName,
                        version: params.commitHash
                    )
                    
                    dockerFunc.pushImage(
                        imageName: params.svcName,
                        version: "stable"
                    )

                    // PyPi versioning
                    // Git tag: stable
                }
            }
        }
        stage("deployment") {
            steps {
                script {
                    k8s.helmConfig()
                    k8s.helmDeploy(
                        release: "${params.svcName}-${params.environment}",
                        chart: "./helm",
                        namespace: params.namespace,
                        version: params.commitHash
                    )
                }
            }
        }
    }
    post {
        always {
            script {
                def SLACK_CHANNEL = "machine-learning-cd"
                def SLACK_TOKEN = "<SLACK_TOKEN>"
                def prependMessage = "Deploying ${params.svcName}\nEnv: ${params.environment}\nNamespace: ${params.namespace}\nCommit: ${params.commitHash}"
                echo 'prependMessage: '+prependMessage
                slackNotifier(currentBuild.currentResult, SLACK_CHANNEL, SLACK_TOKEN, prependMessage)
                currentBuild.description = "Service: ${params.svcName}<br>Tag: ${tag}<br>Env: ${params.environment}<br>Branch: ${params.branch}"
            }
        }
    }
}