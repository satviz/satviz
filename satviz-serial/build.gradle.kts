plugins {
    `java-library`
}

repositories {
    maven("https://repo.devcord.club/snapshots")
}

dependencies {
    implementation(project(":satviz-sat"))
}