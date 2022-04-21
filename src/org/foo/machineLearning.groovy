package org.foo;

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

class GlobVars {
    static String containerName   = 'nodeTemplate'
    static String tag
    static String svcName
}

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
    container("python") {
        if (stageData.cmdOverride && stageData.cmdOverride != "") {
            sh stageData.cmdOverride
        } else {
            sh "bash run_tests.sh \$PWD"
        }
    }
}

def compile(stageData) {
    container("python") {
        if (stageData.cmdOverride && stageData.cmdOverride != "") {
            sh stageData.cmdOverride
        } else {
            sh "bash run_tests.sh \$PWD"
        }
    }
}

def test(stageData) {
    container("python") {
        if (stageData.cmdOverride && stageData.cmdOverride != "") {
            sh stageData.cmdOverride
        } else {
            sh "bash run_tests.sh \$PWD"
        }
    }
}

def tests(stageData) {
    container("python") {
        if (stageData.cmdOverride && stageData.cmdOverride != "") {
            sh stageData.cmdOverride
        } else {
            sh "bash run_tests.sh \$PWD"
        }
    }
}

def artifact(stageData) {
    container("python") {
        if (stageData.cmdOverride && stageData.cmdOverride != "") {
            sh stageData.cmdOverride
        } else {
            sh "bash run_tests.sh \$PWD"
        }
    }
}

def intTest(stageData) {
    container("python") {
        if (stageData.cmdOverride && stageData.cmdOverride != "") {
            sh stageData.cmdOverride
        } else {
            sh "bash run_tests.sh \$PWD"
        }
    }
}

def deployment(stageData) {
    container("python") {
        if (stageData.cmdOverride && stageData.cmdOverride != "") {
            sh stageData.cmdOverride
        } else {
            sh "bash run_tests.sh \$PWD"
        }
    }
}

return this