package org.foo.functions;

def helmDeploy(Map args=[:]) {
    return sh(script: """helm upgrade -i \
                    ${args.release} \
                    ${args.chart} \
                    -n ${args.namespace} \
                    --version ${args.version} \
                    --wait""", returnStdout: true) ? true : false
}

def helmConfig(Map args=[:]) {
    if (!args.chart) args.chart = "bitnami"
    if (!args.chart_url) args.chart_url = "https://charts.bitnami.com/bitnami"
    return sh(script: "helm repo add ${args.chart} ${args.chart_url}", returnStdout: true) ? true : false
}

return this