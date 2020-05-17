import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        def links = variables.collect { k, v -> "--link ${v.id}:${k}" }.join(" ")
        node() {
            dockerImage.inside(links) {
                stepsA.each { step ->
                    node {
                        label 'team_a'
                    }
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

