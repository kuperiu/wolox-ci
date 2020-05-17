import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        def links = variables.collect { k, v -> "--link ${v.id}:${k}" }.join(" ")
node {
    stage("Build") {
        echo "Some code compilation here..."
    }

    stage("Test") {
        echo "Some tests execution here..."
        echo 1
    }
}
            // stepsA.each { step ->
            //     stage(step.name) {
            //         def customImage = docker.image(step.image)
            //         customImage.inside(links) {
            //             step.commands.each { command ->
            //                 sh command
            //             }
            //         }
            //     }
            // }
    }
}
