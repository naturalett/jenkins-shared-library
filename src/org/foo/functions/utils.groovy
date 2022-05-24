package org.foo.functions;

def getProjectLanguage(Map args=[:]) {
    if (!args.containerName) args.containerName = "alpine-curl-jq"
    container(args.containerName) {
        return sh(script: """curl -s \
                            https://api.github.com/repos/naturalett/streamlit-apps/languages \
                            | jq --stream -n 'input[0][]'""", returnStdout: true)
    }
}

return this