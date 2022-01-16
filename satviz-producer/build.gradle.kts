plugins {
    java
    id("org.beryx.jlink") version "2.24.4"
}

repositories {
    maven("https://repo.devcord.club/snapshots")
}

dependencies {
    implementation("edu.kit.satviz:ipasir4j:0.1.0-SNAPSHOT")
    implementation(project(":satviz-common"))
    implementation(project(":satviz-network"))
    implementation(project(":satviz-parsers"))
}
