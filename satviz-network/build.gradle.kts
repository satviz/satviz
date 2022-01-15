plugins {
    `java-library`
}

dependencies {
    implementation(project(":satviz-serial"))
    api(project(":satviz-sat"))
}