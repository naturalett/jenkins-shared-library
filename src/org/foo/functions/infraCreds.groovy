package org.foo.functions;
import groovy.transform.Field

@Field def utils = new org.foo.functions.utils()

def setupCredentials(Map args=[:]) {
    if (!args.projLanguage) args.projLanguage = utils.getProjectLanguage()
    def settings = [
                "Java" : [
                    "creds" : "settings-xml",
                    "container" : "Java",
                    "cmd" : 'echo $credentials > ${MAVEN_HOME}/conf/settings.xml'
                ],
                "Python" : [
                    "creds" : "pip-conf",
                    "container" : "Python",
                    "cmd" : 'echo "$credentials" > /etc/pip.conf && echo "$credentials" > ${WORKSPACE}/pip.conf'
                ]
    ]
    withCredentials([string(credentialsId: settings[args.projLanguage]["creds"], variable: 'credentials')]) {
        sh settings[args.projLanguage]["cmd"]
    }
}

return this