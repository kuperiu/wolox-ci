import org.jenkinsci.plugins.workflow.libs.Library
@Library('wolox-ci')
import com.wolox.*;
import com.wolox.steps.Step;

def call(ProjectConfiguration projectConfig, def dockerImage) {
    return { variables ->
        List<Step> stepsA = projectConfig.steps.steps
        def links = variables.collect { k, v -> "--link ${v.id}:${k}" }.join(" ")
node {
		stage (‘Build’) {
			bat "msbuild ${C:\\Jenkins\\my_project\\workspace\\test\\my_project.sln}"
		}

stage('Selenium tests') {
dir(automation_path) {  //changes the path to “automation_path”
bat "mvn clean test -Dsuite=SMOKE_TEST -Denvironment=QA"
}  
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
