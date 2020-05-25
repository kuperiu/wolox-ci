import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
//import com.wolox.steps.Stage;
import com.wolox.steps.*;
import com.wolox.secrets.Secret;

def vault(service, path, key) { 
    node('team_a') {
        withEnv(["VAULT_NAMESPACE=${service}"]) {
            login = "vault login -method=aws role=${service} > /dev/null 2>&1"
            readSecret = "vault kv get -format=json ${path} | jq .data.data.${key}"
            script {  
                sh(script: login)
                return sh(script: readSecret, returnStdout: true).trim()
            }
        }
    }
}

// Create List of build stages to suit
def prepareBuildStages(sta, steps) {
    def buildStagesList = []
    sta.steps.each { stepName ->
        def buildParallelMap = [:]
        steps.each { s ->
            if (s.name == stepName) {
                buildParallelMap.put(stepName, prepareOneBuildStage(stepName))
            }
        }
        buildStagesList.add(buildParallelMap)
    }

    return buildStagesList

//     stages.each{ sta ->
//         println(sta.name)
//         println(sta.steps)
//         sta.steps.each { stepName ->
//             def buildParallelMap = [:]
//             steps.each { ste ->
//                 if (ste.name == stepName) {
//                     buildParallelMap.put(stepName, prepareOneBuildStage(stepName))
//                 }
//             }
//             buildStagesList.add(buildParallelMap)
//         }
//     }
  
//   return buildStagesList
}

def prepareOneBuildStage(String name) {
  return {
    stage("Build stage:${name}") {
      println("Building ${name}")
      sh(script:'sleep 5', returnStatus:true)
    }
  }
}

def addScmVars(scmVars) {
    scmVars.each{ k,v ->
        env."${k}" = v
    }
}

def postTest(stepName) {
    junit "${stepName}.xml"
    if (currentBuild.result == 'UNSTABLE') {
        currentBuild.result = 'FAILURE'
        throw new Exception("Step ${stepName} has failed")
    }
}


def prepareStage(myStage, stepsA) {
    def parallelSteps = [:]
    for (myStep in myStage.steps) {
        stepsA.eachWithIndex { item, i ->
            if (myStep == item.name) {
                int index=i, branch = i+1
                parallelSteps[stepsA[index].name] = {
                    docker.image(item.image).inside("--entrypoint=''")  {
                        stepsA[index].commands.each { command ->
                            sh command
                        }
                        if (stepsA[index].recordTest) {
                                postTest(stepsA[index].name)
                        }
                        if (stepsA[index].archiveArtifact) {
                                 archiveArtifacts artifacts: 'jenkins', fingerprint: true
                        }
                    }
                }
            }
        }
    }
    parallel(parallelSteps)
    parallelSteps.clear()
}

def prepareDeployment(owner, repo) {
    def ref = "master"
    def environment = env.DEPLOYMENT
    def description = env.RUN_DISPLAY_URL
    def deployURL = "https://api.github.com/repos/${owner}/${repo}/deployments"
    def deployBody = '{"ref": "' + ref +'", "required_contexts": [], "environment": "' + environment  +'","description": "' + description + '" }'
    echo deployBody
    def response = httpRequest authentication: 'github2', httpMode: 'POST', requestBody: deployBody, responseHandle: 'STRING', url: deployURL
    if(response.status != 201) {
        error("Deployment API Create Failed: " + response.status)
    }
    def responseJson = readJSON text: response.content
    def id = responseJson.id
    if(id == "") {
        error("Could not extract id from Deployment response")
    }
    return id
}

def recordDeploymentStatus(owner, repo, result) {
    def deployStatusBody = '{"state": "' + result + '","target_url": "http://github.com/deploymentlogs"}'
    def deployStatusURL = "https://api.github.com/repos/${owner}/${repo}/deployments/${id}/statuses"
    def deployStatusResponse = httpRequest authentication: 'github2', httpMode: 'POST', requestBody: deployStatusBody , responseHandle: 'STRING', url: deployStatusURL
    if(deployStatusResponse.status != 201) {
        error("Deployment Status API Update Failed: " + deployStatusResponse.status)
    }
}

def call(ProjectConfiguration projectConfig, def dockerImage) {
    if (currentBuild.rawBuild.getCauses().toString().contains('BranchIndexingCause')) {
        print "INFO: Build skipped due to trigger being Branch Indexing"
        return { variables ->
            node() {}
        }
    }

    return { variables ->
        List<Stage> stagesA = projectConfig.stages.stages
        List<Step> stepsA = projectConfig.steps.steps
        List<Secret> secrets = projectConfig.secrets.secrets


        def secretList = []
        secrets.each { secret ->
           mySecret = vault(secret.service, secret.path, secret.key)
           secretList << "${secret.name}=${mySecret}"
        } 

       // secretList.add(envVariables)
        label = "team_a"
        def links = '--entrypoint=""'
        def runParallel = true
        def owner = "kuperiu"
        def id = ""
        def jobName = env.JOB_NAME.split("/")
        def repo = jobName[0]

        properties([[$class: 'ParametersDefinitionProperty', parameterDefinitions: [[$class: 'StringParameterDefinition', name: 'DEPLOYMENT', defaultValue: '']]]])

        withEnv(secretList) {
            node(label) {    
                def scmVars = checkout(scm)  
                addScmVars(scmVars)
                 if (env.DEPLOYMENT != "" && env.GIT_BRANCH == "master") {
                     id = prepareDeployment(owner, repo)
                 }
                for (myStage in stagesA) {
                    if (env.DEPLOYMENT != "" && env.GIT_BRANCH == "master") {
                        stage(myStage.name) {
                            prepareStage(myStage, stepsA)
                        }      
                    }
                    if (env.GIT_BRANCH == myStage.branch || myStage.branch == null) {
                        stage(myStage.name) {
                            prepareStage(myStage, stepsA)
                        }                      
                    }
                }

                if (env.DEPLOYMENT != "" && env.GIT_BRANCH == "master") {
                    if (currentBuild.result != null) {
                        recordDeploymentStatus(owner, repo, "success")
                    } else {
                        recordDeploymentStatus(owner, repo, "failure")
                    }        
                }
            }
        }
    }
}
