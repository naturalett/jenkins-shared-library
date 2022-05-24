package org.foo.functions;
import groovy.transform.Field

@Field def utils = new org.foo.functions.utils()

def setupCredentials(Map args=[:]) {
    if (!args.projLanguage) args.projLanguage = utils.getProjectLanguage()
    def settings = [
                "Java" : [
                    "creds" : "settings-xml",
                    "container" : "Java",
                    "cmd" : '{ echo $credentials > ${MAVEN_HOME}/conf/settings.xml; } 2> /dev/null'
                ],
                "Python" : [
                    "creds" : "pip-conf",
                    "container" : "Python",
                    "cmd" : '{ echo "$credentials" > /etc/pip.conf; } 2> /dev/null && { echo "$credentials" > ${WORKSPACE}/pip.conf; } 2> /dev/null'
                ]
    ]
    withCredentials([string(credentialsId: settings[args.projLanguage]["creds"], variable: 'credentials')]) {
        sh settings[args.projLanguage]["cmd"]
    }
}

return this