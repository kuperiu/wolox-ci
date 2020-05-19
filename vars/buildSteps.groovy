import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
//import com.wolox.steps.Stage;
import com.wolox.steps.*;
import com.wolox.secrets.Secret;

def vault(service, path, key) { 
    node('team_a') {
        withEnv(["VAULT_ADDR=https://this.vault.dazn-dev.com", "VAULT_NAMESPACE=${service}"]) {
            login = "vault login -method=aws role=${service} > /dev/null 2>&1"
            readSecret = "vault kv get -format=json ${path} | jq .data.data.${key}"
            script {  
                sh(script: login)
                return sh(script: readSecret, returnStdout: true).trim()
            }
        }
    }
}

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Stage> stagesA = projectConfig.stages.stages
        List<Step> stepsA = projectConfig.steps.steps
        List<Secret> secrets = projectConfig.secrets.secrets
        def secretList = []
        secrets.each { secret ->
           mySecret = vault(secret.service, secret.path, secret.key)
           secretList << "${secret.name}=${mySecret}"
        } 

        label = "team_a"
        def links = '--entrypoint=""'
        def runParallel = true
        def buildStages

        withEnv(secretList) {
            node() {
                stagesA.each { sta ->
                    stage(sta.name) {
                        sta.steps.each { stepName ->
                            println(stepName)
                        }
                    }
                }
            }
        }
    }
}
        // withEnv(secretList) {
        //     node() {
        //         stagesA.each { s ->
        //                 parallel 'linux': {
        //                     stage('Linux') {
        //                        println("Linux")
        //                     }
        //                 }, 'windows': {
        //                     stage('Windows') {
        //                         println("Windows")
        //                     }
        //                 }
        //             }

        //         }
        //     }
// node('master') {
//   stage('Initialise') {
//     // Set up List<Map<String,Closure>> describing the builds
//     buildStages = prepareBuildStages()
//     println("Initialised pipeline.")
//   }

//   for (builds in buildStages) {
//     if (runParallel) {
//       parallel(builds)
//     } else {
//       // run serially (nb. Map is unordered! )
//       for (build in builds.values()) {
//         build.call()
//       }
//     }
//   }

//   stage('Finish') {
//       println('Build complete.')
//   }
// }
            // stagesA.each { s ->
            //     node(label) {
            //         stage(s.name) {
            //             s.steps.each {
            //                 def step = getStep(stepsA, it)
            //                 // parallel (
            //                 //     "${step.name}": {
            //                 //         node(label) {
            //                 //             docker.image(step.image).inside("--entrypoint=''")  {
            //                 //                 step.commands.each { command ->
            //                 //                     sh command
            //                 //                 }
            //                 //             }   
            //                 //         }
            //                 //     }
            //                 // )
            //             }
            //         }
            //     }
            // }
    //     }
    // }
// }

def getStep(Steps steps, String name) {
    Step step = new Step()
    steps.each { k, v ->
        if (k == name) {
            step.set(k, v.image, v.commands)
        }
    }
    return step
}

// Create List of build stages to suit
def prepareBuildStages() {
  def buildStagesList = []

  for (i=1; i<5; i++) {
    def buildParallelMap = [:]
    for (name in [ 'one', 'two', 'three' ] ) {
      def n = "${name} ${i}"
      buildParallelMap.put(n, prepareOneBuildStage(n))
    }
    buildStagesList.add(buildParallelMap)
  }
  return buildStagesList
}

def prepareOneBuildStage(String name) {
  return {
    stage("Build stage:${name}") {
      println("Building ${name}")
      sh(script:'sleep 5', returnStatus:true)
    }
  }
}



// def calling(ProjectConfiguration projectConfig, def dockerImage) {
//     return { variables ->
//         List<Step> stepsA = projectConfig.steps.steps
//         List<Secret> secrets = projectConfig.secrets.secrets
//         def secretList = []
//         secrets.each { secret ->
//            mySecret = vault(secret.service, secret.path, secret.key)
//            secretList << "${secret.name}=${mySecret}"
//         } 


//         //def links = variables.collect { k, v -> "--entrypoint="" --link ${v.id}:${k}" }.join(" ")
//         label = "team_a"
//         def links = '--entrypoint=""'
//         withEnv(secretList) {
//             stepsA.each { step ->
//                 node(label) {
//                     stage "Start"
//                     parallel (
//                         "${step.name}": {
//                             node(label) {
//                                 docker.image(step.image).inside("--entrypoint=''")  {
//                                      step.commands.each { command ->
//                                         sh command
//                                      }
//                                 }   
//                             }
//                         }
// //   'Build' : {
// //     node {
// //       git url: 'http://github.com/karlkfi/minitwit'
// //       sh 'ci/build.sh'
// //     }
// //   },
// //   'Test' : {
// //     node {
// //       git url: 'http://github.com/karlkfi/minitwit'
// //       sh 'ci/test-unit.sh'
// //     }
// //   }
//                         // stage(step.name) {
//                         //     // def customImage = docker.image(step.image)
//                         //         docker.image(step.image).inside("--entrypoint=''")  {
//                         //             step.commands.each { command ->
//                         //                 sh command
//                         //             }
//                         //         }
//                         //     }
//                     )
// node(label) {
//     stage 'middle' {

//     }
// }
//                 }
//             }
//         }
//     }
// }

