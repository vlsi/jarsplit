plugins {
    id("java-library")
    id("application")
}

group = "org.example"
version = "1.0.0"

dependencies {
    implementation("org.example:svg:1.0.0")
    implementation("org.example:http:1.1.0")
}

application {
    mainClass.set("org.example.app.Main")
}

repositories {
    maven {
        name = "repo"
        url = uri("../repo")
    }
}
