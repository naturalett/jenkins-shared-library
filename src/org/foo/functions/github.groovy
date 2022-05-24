package org.foo.functions;

def getGitAuthor(Map args=[:]) {
    if (!args.containerName) args.containerName = "git"
    container(args.containerName) {
        return sh (script: 'git log -1 --pretty=%cn ${GIT_COMMIT}', returnStdout: true).trim()
    }
}

return this