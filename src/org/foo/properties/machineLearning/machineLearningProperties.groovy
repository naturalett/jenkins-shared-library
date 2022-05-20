package org.foo.properties.machineLearning;

def extendedParametersManualJobs(Map args=[:]) {
    def environmentInfo = this.environmentInfo()
    return  properties([
        disableConcurrentBuilds(),
        parameters([
                choice(choices: ['production', 'staging', 'playground'], description: 'Choose the env i.e. production, staging or playground', name: 'environment'),
                (args.namespace?args.namespace : string(name: 'namespace', defaultValue: 'Default namespace is configured', description: 'Not Validate') ),
                string(name: 'commitHash', defaultValue: 'master', description: 'Clone a specific repository by a commitHash<br>${environmentInfo}')
            ])
        ])
}

def environmentInfo() {
    def environmentURLLinks = """
        <p style="font-size:50px;color:blue;">Environment information:</p>
    """
    return environmentURLLinks
}

def machineLearningExtendedParameters(extraProperties="") {
    return this.extendedParametersManualJobs(
        namespace: choice(choices: ['machine-learning'], description: 'Choose the namespace', name: 'namespace')
    )
}

return this