package org.foo.functions;

def helmDeploy(Map args) {
    //configure helm client and confirm tiller process is installed
    helmConfig()

    def overrides = "image.tag=${args.version_tag},track=staging,branchName=${args.branch_name},branchSubdomain=${args.branch_name}."
    def releaseName = shortenLongReleaseName(args.branch_name, args.name)

    // Master for prod deploy w/o ingress (using it's own ELB)
    if (args.branch_name == 'master') {
      overrides = "${overrides},ingress.enabled=false,track=stable,branchSubdomain=''"
    }

    if (args.dry_run) {
        println "Running dry-run deployment"

        sh "helm upgrade --dry-run --install ${releaseName} ${args.chart_dir} --set ${overrides} --namespace=${args.namespace}"
    } else {
        println "Running deployment"
        sh "helm upgrade --install --wait ${releaseName} ${args.chart_dir} --set ${overrides} --namespace=${args.namespace}"

        echo "Application ${args.name} successfully deployed. Use helm status ${args.name} to check"
    }
}

def helmConfig() {
    //setup helm connectivity to Kubernetes API and Tiller
    println "initiliazing helm client"
    sh "helm init"
    println "checking client/server version"
    sh "helm version"
}

return this