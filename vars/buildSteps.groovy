import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def vault() { 
    withEnv(["VAULT_ADDR=https://this.vault.dazn-dev.com", "VAULT_NAMESPACE=test-lior2"]) {
        script {  
            sh(script: 'vault login -method=aws role=test-lior2')
            return sh(script : 'vault kv get -format=json kv/dev | jq .data.data.env', returnStdout: true).trim()
        }
    }
}

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        //def links = variables.collect { k, v -> "--entrypoint="" --link ${v.id}:${k}" }.join(" ")
        def links = '--entrypoint=""'
        def secret = vault()
        withEnv(["SECRET=${secret}"]) {
            stepsA.each { step ->
                stage(step.name) {
                   // def customImage = docker.image(step.image)
                    docker.image(step.image).inside("--entrypoint=''")  {
                        step.commands.each { command ->
                            echo "${env.SECRET}"
                            sh command
                        }
                    }
                }
            }
        }
    }
}

