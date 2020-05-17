import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        def links = variables.collect { k, v -> "--link ${v.id}:${k}" }.join(" ")
            stepsA.each { step ->
                stage(step.name) {
                    print "yyyyyyyy"
                    print step.name
                    print step.image
                    def customImage = docker.image(step.image)
                    customImage.inside(links) {
                        step.commands.each { command ->
                            sh command
                        }
                    }
                }
            }
    }
}
