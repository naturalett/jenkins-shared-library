# Using dependencies images in Jenkinsfile
* [How to use](#how-to-use)

## How to use
File: `org/foo/infraTemplate.yaml`

### Default vars
* nodeSelectorName: buildnodes
* **node_image_repository:** node, **node_version:** fermium
* **mongo_repository:** mongo, **mongo_version:** '4.2'
 
### Using image repository for node version
```
def template_vars = [
    'node_image_repository': '<organization>/<repo_name>',
    'node_version' :'your-customize-version'
]
```

#### Define the template_vars that you want to change:
If you don't add **node_image_repository** then the default repository is [node][docker-hub-node-version].
In the below example the **node_image_repository** is [node][docker-hub-node-version] and the version in that repository is **fermium**
```
def template_vars = [
    'nodeSelectorName': 'buildnodes',
    'node_version' :'fermium',
    'image_dependencies' : [python, java]
]
```

image_dependencies will get extra libraryResources such as:
  * def python = libraryResource 'org/foo/dependencies/python.yaml'
  * def java = libraryResource 'org/foo/dependencies/java.yaml'

#### Full example of Jenkinsfile
```
@Library('ni-utils') _

//service name is extrapolated from repository name check
def svcName = currentBuild.rawBuild.project.parent.displayName

def pod = libraryResource 'org/foo/nodeTemplate.yaml'
def python = libraryResource 'org/foo/dependencies/python.yaml'
def template_vars = [
    'nodeSelectorName': 'buildnodes',
    'build_label': svcName,
    'node_version' :'alpine',
    'image_dependencies' : [python]
]
pod = renderTemplate(pod, template_vars)
def sharedLibrary = new org.foo.machineLearning() 

def initiateData = [project: "ML"]
def compileData = [run: true]
def testData = [run: true]
def artifactData = [run: true, uploadTo: ["PyPi","dockerhub"]]
def intTestData = [run: true]
def deploymentData = [run: true]
def buildCommands = [
    initiateData: initiateData,
    compileData: compileData,
    testData: testData,
    artifactData: artifactData,
    intTestData: intTestData,
    deploymentData: deploymentData
]

// Set slack channel
def slackChannel = "k8s-jenkins"

timestamps {
    commonPipeline(sharedLibrary, svcName, buildCommands, pod, slackChannel)
}
```

[docker-hub-node-version]: <https://hub.docker.com/_/node?tab=tags&page=1&ordering=last_updated>
