package org.foo.pipelines;
import groovy.transform.Field

// https://docs.groovy-lang.org/3.0.7/html/gapi/groovy/transform/Field.html
// Variable annotation used for changing the scope of a variable within a script from being within the run method of the script to being at the class level for the script.

/**
 * Upload python package
 * Upload docker image

 Pipeline:
    - Initialization
    - Compilation
    - Unit Tests
    - Build
    - Artifact
    - Integration Tests
    - Deployment
 */

@Field String svcName = currentBuild.rawBuild.project.parent.displayName
@Field String containerName = 'python'
@Field String organization = "naturalett"
@Field String repository = "machine-learning"
@Field String tag
@Field def k8s = new org.foo.functions.k8s()

def executeStage(stageName, stageData, tag="") {
    switch (stageName) {
        case "initializaion": 
            this.initializaion(stageData)
            break; 
        case "compile": 
            this.compile(stageData)
            break; 
        case "test":
            this.test(stageData)
            break;
        case "artifact":
            this.artifact(stageData)
            break; 
        case "int-test": 
            this.intTest(stageData)
            break;
        case "deployment": 
            this.deployment()
            break; 
    }
}

def initializaion(stageData) {
    GlobVars.tag = checkout(scm).GIT_COMMIT[0..6]
}

def compile(stageData) {
    container(containerName) {
        def image = docker.build("${organization}/${repository}")
    }
}

def test(stageData) {
    container(containerName) {
        args = "python unittests.py"
        status =  image.inside { c ->
            sh (script: args, returnStatus: true)
        }
    }
}

def artifact(stageData) {
    container(containerName) {
        docker.withRegistry('https://index.docker.io/v1/', 'dockerlogin') {
            image.push("latest")
            image.push(tag)
        }
    }
}

def intTest(stageData) {
    echo "TODO"
}

def deployment(stageData) {
    container(containerName) {
        k8s.helmConfig(
            chart: "bitnami",
            chart_url: "https://charts.bitnami.com/bitnami"
        )
        k8s.helmDeploy(
            release: "prometheus",
            chart: "bitnami/kube-prometheus",
            namespace: "default",
            version: "6.9.6"
        )
    }
}

return this