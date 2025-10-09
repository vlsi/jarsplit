#!/bin/sh

(cd v1; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd v2; ./gradlew publishAllPublicationsToRepoRepository publishToMavenLocal)
(cd lib-user-gradle; ./gradlew dependencies --configuration runtimeClasspath)
(cd lib-user-maven; mvn dependency:tree)
(cd lib-user-maven4; ./mvnw dependency:tree)
