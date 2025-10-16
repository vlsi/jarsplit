plugins {
    id("java-library")
    id("maven-publish")
}

allprojects {
    group = "org.example"
    version = "1.0.0"

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

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
}
