package com.wolox.steps;

class Step {
    List<String> commands = [];
    String name;
    String image;
    Bool recordTest;
    Bool archiveArtifact;

    def set(String name, String image, List<String> commands) {
        this.name = name
        this.image = image
        this.commands = commands
    }

    def getCommands() {
        return commands
    }
}
