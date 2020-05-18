import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def vault(service, path, key) { 
    withEnv(["VAULT_ADDR=https://this.vault.dazn-dev.com", "VAULT_NAMESPACE=${service}"]) {
        script {  
            sh(script: 'vault login -method=aws role=test-lior2', returnStdout: false)
            return sh(script: 'vault kv get -format=json kv/dev | jq .data.data.'"${key}"'', returnStdout: true).trim()
        }
    }
}

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        //def links = variables.collect { k, v -> "--entrypoint="" --link ${v.id}:${k}" }.join(" ")

        def links = '--entrypoint=""'
        def secret = vault("test-lior2", "kv/dev", env)
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

