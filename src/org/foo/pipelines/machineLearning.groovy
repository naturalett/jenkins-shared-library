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
@Field String containerName = 'docker', organization = "naturalett"
@Field Boolean openSource = true
@Field String tag, namespace, author, image
@Field def k8s = new org.foo.functions.k8s()
@Field def dockerActions = new org.foo.functions.docker()
@Field def git = new org.foo.functions.github()
@Field def creds = new org.foo.functions.infraCreds()

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
    namespace = stageData.namespace
    tag = checkout(scm).GIT_COMMIT[0..6]
    echo "Tag: ${tag}"
    return tag
}

def compile(stageData) {
    container(containerName) {
        creds.setupCredentials()
        image = dockerActions.buildImage(
            imageName: "${organization}/${svcName}"
        )
        // Alternative way to build the image: image = docker.build("${organization}/${svcName}")
    }
}

def test(stageData) {
    container(containerName) {
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
        if (openSource) { k8s.helmConfig()}
        stageData.environments { environment ->
            k8s.helmDeploy(
                release: "${svcName}-${environment}",
                chart: "./helm",
                namespace: "default",
                version: "1.0.0"
            )
        }
    }
}

def successStep() {
    for (environment in ["staging", "production"]) {
        def url = env.JENKINS_URL + "/job/Deploy_Application/parambuild" + \
        "?environment=${environment}" + \
        "&namespace=${namespace}" + \
        "&commitHash=${tag}"
        addBadge(icon : "success.gif", text: "Deploy to ${environment}", link: url)
    }
    author = git.getGitAuthor(
        commit: tag
    )
    currentBuild.description = "Commit: ${tag}<br>Author: ${author}"
}

return this