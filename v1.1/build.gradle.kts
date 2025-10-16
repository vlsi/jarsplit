plugins {
    id("java-platform")
    id("maven-publish")
}

allprojects {
    group = "org.example"
    version = "1.1.0"
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(project("commons-compress-core"))
    api(project("commons-compress-tar"))
    api(project("commons-compress-xz"))
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
}

allprojects {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components[if (project.parent == null) "javaPlatform" else "java"])
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
