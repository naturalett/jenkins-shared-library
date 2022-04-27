package org.foo.pipelines;
import groovy.transform.Field

// https://docs.groovy-lang.org/3.0.7/html/gapi/groovy/transform/Field.html
// Variable annotation used for changing the scope of a variable within a script from being within the run method of the script to being at the class level for the script.
// https://artifacthub.io/packages/helm/linzhengen/web

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

@Field String svcName = (scm.getUserRemoteConfigs()[0].getUrl().tokenize('/')[3].split("\\.")[0]).toLowerCase()
@Field String containerName = 'docker'
@Field String organization = "naturalett"
@Field Boolean openSource = true
@Field String tag
@Field String image
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
    tag = checkout(scm).GIT_COMMIT[0..6]
    echo "Tag: ${tag}"
    return tag
}

def compile(stageData) {
    container(containerName) {
        image = docker.build("${organization}/${svcName}")
    }
}

def test(stageData) {
    container('python') {
        args = "python -m unittest"
        status =  image.inside { c ->
            sh (script: args, returnStatus: true)
        }
    }
}

def artifact(stageData) {
    container(containerName) {
        stageData.artifactType.each { artifactType ->
            switch (artifactType) {
                case "DockerHub":
                    docker.withRegistry('https://index.docker.io/v1/', 'dockercred') {
                        if (["master", "main"].contains(GIT_BRANCH)) { image.push("latest") }
                        image.push(tag)
                    }
                    break;
                case "PyPi":
                    echo "TODO"
                    break;
            }
        }
    }
}

def intTest(stageData) {
    echo "TODO"
}

def deployment(stageData) {
    container(containerName) {
        if (openSource) {
            k8s.helmConfig(
                chart: "t3n",
                chart_url: "https://storage.googleapis.com/t3n-helm-charts"
            )
        }
        stageData.environments { environment ->
            k8s.helmDeploy(
                release: "${svcName}-${environment}",
                chart: "t3n/web",
                namespace: "default",
                version: "1.0.0"
            )
        }
    }
}

return this