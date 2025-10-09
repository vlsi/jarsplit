plugins {
    id("java-library")
}

group = "org.example"
version = "1.0.0"

dependencies {
    implementation("org.example:commons-compress:1.0.0")
    implementation("org.example:commons-compress-tar:2.0.0")
}

repositories {
    maven {
        name = "repo"
        url = uri("../repo")
    }
}
