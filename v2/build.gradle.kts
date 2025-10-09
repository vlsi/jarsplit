plugins {
    id("java-platform")
    id("maven-publish")
}

allprojects {
    group = "org.example"
    version = "2.0.0"
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(project("commons-compress-core"))
    api(project("commons-compress-tar"))
    api(project("commons-compress-xz"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
    repositories {
        maven {
            name = "repo"
            url = uri(rootProject.layout.projectDirectory.dir("../repo"))
        }
    }
}
