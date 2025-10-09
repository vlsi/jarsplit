plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    constraints {
        api(platform(project(":")))
    }
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
