// imports
import hudson.plugins.git.*;
import jenkins.model.Jenkins
import hudson.model.ListView
import groovy.json.JsonSlurper


def projectNames = [
  "machineLearning"
]

def sharedLibrariesName = "jenkins-shared-libraries"
def sharedLibrariesUrl = "https://github.com/naturalett/jenkins-shared-libraries.git"

// Authentication
def fetch(addr, params = [:]) {
  def auth = "<GITHUB_AUTH>"
  def json = new JsonSlurper()
  return json.parse(addr.toURL().newReader(requestProperties: [
    "Authorization": "token ${auth}".toString(),
    "Accept": "application/json"
  ]))
}

// Get Jenkins instance
parent = Jenkins.instance

// Define git repository
scm = new GitSCM(sharedLibrariesUrl)
scm.userRemoteConfigs = scm.createRepoList(sharedLibrariesUrl, "<GITHUB_TOKEN>")
scm.branches = [new BranchSpec("*/master")];

for (projectName in projectNames) {
  fetch("https://api.github.com/repos/naturalett/${sharedLibrariesName}/contents/jobs/${projectName}").each{ item ->
    item_name = item.name.toString()
    jobName = item_name.replace(".groovy","").replace("Jenkinsfile.","").replace(".","_")
    flowDefinition = new org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition(scm, "jobs/${projectName}/${item_name}")

    // Create a view and add job to it
    viewName = jobName.split("_")[0]
    myView = hudson.model.Hudson.instance.getView(viewName)
    if (!myView){
      parent.addView(new ListView(viewName))
      myView = hudson.model.Hudson.instance.getView(viewName)
    }

    // Add job to view if it doesnt exist
    myJob = hudson.model.Hudson.instance.getJob(jobName)
    if (!myJob) {
       println("${jobName} is being created")
       job = new org.jenkinsci.plugins.workflow.job.WorkflowJob(parent, jobName)
       job.definition = flowDefinition
       parent.reload()
    } else {
        println("${myJob} already exist")
    }
    myView.doAddJobToView(jobName)
  }
}

