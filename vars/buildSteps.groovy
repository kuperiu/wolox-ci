import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        def links = variables.collect { k, v -> "--link ${v.id}:${k}" }.join(" ")
        stage('Test on Linux') {
            agent {
                label 'team-a'
            }
        dockerImage.inside(links) {
            stepsA.each { step ->
                stage(step.name) {
                    step.commands.each { command ->
                        sh command
                    }
                }
            }
        }
        }
    }
}
