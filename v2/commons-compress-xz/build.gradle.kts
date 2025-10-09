plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    api(project(":commons-compress-core"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "repo"
            url = uri(rootProject.layout.projectDirectory.dir("../repo"))
        }
    }
}
