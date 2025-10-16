#!/bin/sh

(cd v1.0; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal) && \
(cd v1.1; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal) && \
(cd app-gradle; ./gradlew dependencies --configuration runtimeClasspath run) && \
(cd app-maven4; ./mvnw -V dependency:tree verify)

#(cd app-maven; ./mvnw -V dependency:tree verify) && \
