package org.foo.functions;

def getProjectLanguage(Map args=[:]) {
    if (!args.containerName) args.containerName = "alpine-curl-jq"
    container(args.containerName) {
        return sh(script: """curl -s \
                            https://api.github.com/repos/naturalett/streamlit-apps/languages \
                            | jq --stream -n 'input[0][]' \
                            | tr -d '"' \
                            | tr -d '\n' """, returnStdout: true)
    }
}

return this