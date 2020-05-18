package com.wolox.steps;

class Secrets {
    List<Secret> secrets;

    def getVar(def dockerImage) {
        return "buildSecrets"
    }
}
