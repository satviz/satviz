plugins {
    java
    id("org.beryx.jlink") version "2.24.4"
}

repositories {
    maven("https://repo.devcord.club")
}

dependencies {
    implementation("edu.kit.satviz:ipasir4j:0.1.0")
    implementation(project(":satviz-common"))
    implementation(project(":satviz-network"))
    implementation(project(":satviz-parsers"))
}
