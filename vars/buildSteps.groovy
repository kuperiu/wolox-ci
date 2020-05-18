import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;
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
        List<Step> stepsA = projectConfig.steps.steps
        List<Secret> secrets = projectConfig.secrets.secrets
        def secretList = []
        secrets.each { secret ->
           mySecret = vault(secret.service, secret.path, secret.key)
           secretList << "${secret.name}=${mySecret}"
        } 


        //def links = variables.collect { k, v -> "--entrypoint="" --link ${v.id}:${k}" }.join(" ")

        def links = '--entrypoint=""'
        withEnv(secretList) {
            stepsA.each { step ->
                node('team_a') {
                    stage "Start"
                    parallel (
                        stage(step.name) {
                            sh 'echo "lolo"'
                            // def customImage = docker.image(step.image)
                                // docker.image(step.image).inside("--entrypoint=''")  {
                                //     step.commands.each { command ->
                                //         sh command
                                //     }
                                // }
                            }
                    )
                }
            }
        }
    }
}

