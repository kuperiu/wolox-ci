import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def vault(service, path, key) { 
    withEnv(["VAULT_ADDR=https://this.vault.dazn-dev.com", "VAULT_NAMESPACE=${service}"]) {
        login = "vault login -method=aws role=${service}"
        readSecret = "vault kv get -format=json ${path} | jq .data.data.${key}"
        script {  
            sh(script: login, returnStdout: false)
            return sh(script: readSecret, returnStdout: true).trim()
        }
    }
}

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        //def links = variables.collect { k, v -> "--entrypoint="" --link ${v.id}:${k}" }.join(" ")

        def links = '--entrypoint=""'
        def secret = vault("test-lior2", "kv/dev", "env")
        withEnv(["SECRET=${secret}"]) {
            stepsA.each { step ->
                stage(step.name) {
                   // def customImage = docker.image(step.image)
                    docker.image(step.image).inside("--entrypoint=''")  {
                        step.commands.each { command ->
                            sh command
                        }
                    }
                }
            }
        }
    }
}

