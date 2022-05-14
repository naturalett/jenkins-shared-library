package org.foo.functions;

def getProjectLanguage(Map args) {
    return sh(script: """curl -s \
                        https://api.github.com/repos/naturalett/streamlit-apps/languages \
                        | jq --stream -n 'input[0][]'""", returnStdout: true)
}

return this