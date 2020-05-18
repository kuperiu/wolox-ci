import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        //def links = variables.collect { k, v -> "--entrypoint="" --link ${v.id}:${k}" }.join(" ")
        def links = '--entrypoint=""'
            stepsA.each { step ->
                stage(step.name) {
                   // def customImage = docker.image(step.image)
                    docker.image("amazon/aws-cli").inside("--entrypoint=''")  {
                        step.commands.each { command ->
                            echo command
                            sh command
                        }
                    }
                }
            }
    }
}

