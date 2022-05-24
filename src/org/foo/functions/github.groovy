package org.foo.functions;

def getGitAuthor(Map args=[:]) {
    if (!args.containerName) args.containerName = "git"
    container(args.containerName) {
        sh (script: 'git config --global --add safe.directory ${WORKSPACE}', returnStdout: true).trim()
        return sh (script: 'git log -1 --pretty=%cn ${GIT_COMMIT}', returnStdout: true).trim()
    }
}

return this