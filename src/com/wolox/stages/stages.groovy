package com.wolox.stages;

class Stage {
    List<Stage> stages;

    def getVar(def dockerImage) {
        return "buildSteps"
    }
}
