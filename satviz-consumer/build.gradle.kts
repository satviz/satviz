plugins {
    java
    id("org.beryx.jlink") version "2.24.4"
}

dependencies {
    implementation(project(":satviz-config"))
    implementation(project(":satviz-gui"))
    implementation(project(":satviz-processing"))
    implementation(project(":satviz-parsers"))
}
