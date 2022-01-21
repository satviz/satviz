plugins {
    `java-library`
}

repositories {
    maven("https://repo.devcord.club/snapshots")
}

dependencies {
    implementation("edu.kit.satviz:ipasir4j:0.1.0-SNAPSHOT")
    implementation(project(":satviz-sat"))
}