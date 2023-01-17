### Architecture diagram
![](examples/architecture_diagram.png)
```bash
helm repo add jenkins https://charts.jenkins.io
helm repo update
helm upgrade -i workshop -f jenkins.yaml --version 4.1.11 jenkins/jenkins
kubectl port-forward svc/workshop-jenkins 8080:8080
kubectl exec --namespace default -it svc/workshop-jenkins -c jenkins -- /bin/cat /run/secrets/additional/chart-admin-password && echo
```

```bash
helm upgrade -i workshop --set persistence.existingClaim=jenkins-pv-claim -f jenkins.yaml jenkins/jenkins
```

```bash
kubectl get secrets workshop-jenkins -oyaml -ojsonpath='{.data.jenkins-password}' | base64 -D
helm upgrade -i workshop --set persistence.existingClaim=jenkins-pv-claim --set service.extraPorts=50000 --set volumePermissions.enabled=true bitnami/jenkins
```


Kubernetes: \
    1. `kubectl apply -f ~/Repos/private/jenkins-shared-library/cluster-role-binding.yaml` \
    2. `kubectl create serviceaccount jenkins`

Github: \
    1. `Create a personal token: https://github.com/settings/tokens` \
    1.1 `Note: Jenkins. Scope: repo.`

Jenkins: \
    1. Add credentials: http://localhost:8080/credentials/store/system/domain/_/
```bash
# add pip-conf
echo -n "[global]
index-url = https://pypi.python.org/simple" | base64 | pbcopy
```
    1.1 Username: github user
    1.2 Password: github token from previous step
    1.3 ID: github
    1.4 Description: github

Jenkins:
* New Item -> Organization Folder
* Owner: naturalett
* Projects:
* Behaviours:
    * Repositories: Filter by name (with regular expression)
    * Within repository: Discover branches, Discover pull requests from origin
        * Add the pipeline libraries
    * Choose the credentials from the previous step
    * Run the job:
        * Approve scripts: 
        ```
        import org.jenkinsci.plugins.scriptsecurity.scripts.*;
        scriptApproval = ScriptApproval.get()
        
        // add all manual whitelist methods here.
        scriptToApprove = [
            "new groovy.text.StreamingTemplateEngine",
            "method groovy.text.TemplateEngine createTemplate java.lang.String",
            "method groovy.text.Template make java.util.Map",
            "method groovy.lang.GroovyObject getMetaClass",
            "method groovy.lang.MetaObjectProtocol respondsTo java.lang.Object java.lang.String"
            "method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object"
        ]
        scriptToApprove.each { script ->
            approveSignature(script)
        }
        def approveSignature(String signature) {
        	scriptApproval.approveSignature(signature)
        }
        /*
        signature : new groovy.text.StreamingTemplateEngine
        method groovy.text.TemplateEngine createTemplate java.lang.String
        method groovy.text.Template make java.util.Map
        method groovy.lang.GroovyObject getMetaClass
        method groovy.lang.MetaObjectProtocol respondsTo java.lang.Object java.lang.String
        */
        ```




    * Add kubernetes cloud agent: http://localhost:8080/configureClouds/
        * Install plugins: http://localhost:8080/pluginManager/available?filter=Cloud+Providers
            * Install (Go to advanced):
                * kubernetes-client: https://updates.jenkins.io/download/plugins/kubernetes-credentials/0.9.0/kubernetes-credentials.hpi
                * kubernetes-credentials: https://updates.jenkins.io/download/plugins/kubernetes-credentials-provider/0.18-1/kubernetes-credentials-provider.hpi
                * metrics: https://updates.jenkins.io/download/plugins/metrics/4.1.6.2/metrics.hpi
                * kubernetes: https://updates.jenkins.io/download/plugins/kubernetes/3622.va_9dc5592b_10c/kubernetes.hpi



    * Configure Global Security
        * Authorization: Anyone can do anything

```yaml
    Add kubernetes cloud agent: http://localhost:8080/configureClouds/
    Kubernetes cloud details:
        Kubernetes urls: https://kubernetes.default
        Kubernetes namespace: default
        Jenkins url: http://my-release-jenkins:8080
        Jenkins tunnel: my-release-jenkins.default.svc.cluster.local:50000
        Add pod labels:
            key: jenkins/jenkins-jenkins-slave
            value: true
        Pod template:
            Advanced -> Add Pod Template:
                name: default
                Pod template details:
                    labels: jenkins-jenkins-slave
                    Usage: use this node as much as possible
                Containers -> Add container:
                    name: jnlp
                    Docker image: jenkins/inbound-agent:4.3-4
                    Arguments to pass to the command: ${computer.jnlpmac} ${computer.name}
                Environment variable -> to add:
                    key: JENKINS_URL
                    value: http://jenkins.jenkins.svc.cluster.local:80/
```



```yaml
# draft
def test(stageData) {
    container(containerName) {
      image.withRun {petclinic ->
        image.inside("--entrypoint python") {
            sh "python -m unittest"
        }
      }
    }
}
```
