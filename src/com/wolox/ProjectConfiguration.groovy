package com.wolox;

import com.wolox.docker.DockerConfiguration;
import com.wolox.steps.Steps;
import com.wolox.secrets.Secrets;
import com.wolox.secrets.Stages;

class ProjectConfiguration {
    def environment;
    def services;
  //  Steps steps;
    Secrets secrets;
    Stages stages;
 //   def dockerfile;
    def image;
    def projectName;
    def buildNumber;
    DockerConfiguration dockerConfiguration;
    def env;
    def timeout;
}
