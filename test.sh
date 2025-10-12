#!/bin/sh

(cd v1; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd v2; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd lib-user-gradle; ./gradlew dependencies --configuration runtimeClasspath run)
(cd lib-user-maven; ./mvnw -V dependency:tree verify)
(cd lib-user-maven4; ./mvnw -V dependency:tree verify)
