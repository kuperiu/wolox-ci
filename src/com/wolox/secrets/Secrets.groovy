package com.wolox.secrets;

class Secrets {
    List<Secret> secrets;

    def getVar(def dockerImage) {
        return "buildSecrets"
    }
}
