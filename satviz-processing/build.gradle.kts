plugins {
    `java-library`
}

dependencies {
    api(project(":satviz-network"))
    implementation(project(":satviz-graph"))
    implementation(project(":satviz-sat"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}