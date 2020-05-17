import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        def links = variables.collect { k, v -> "-it --link ${v.id}:${k}" }.join(" ")
        pipeline {
            stages {
                stage('clone') {
                    steps {
                        sh "ls -la"
                    }
                }
            }
        }
            // stepsA.each { step ->
            //     stage(step.name) {
            //                     node {
            //     docker { image 'maven:3-alpine' }
            // }
            // steps {
            //     sh 'mvn --version'
            // }
            //         // def customImage = docker.image(step.image)
            //         // customImage.inside(links) {
            //         //     step.commands.each { command ->
            //         //         sh command
            //         //     }
            //         // }
            //     }
            // }
    }
}
