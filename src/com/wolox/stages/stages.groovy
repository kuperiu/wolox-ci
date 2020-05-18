package com.wolox.stages;
import com.wolox.steps.*;

class Stages {
    List<Stage> stages;

    def getVar(def dockerImage) {
        return "buildStages"
    }
}
