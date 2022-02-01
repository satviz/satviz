plugins {
    java
    id("org.beryx.jlink") version "2.24.4"
}

repositories {
    maven("https://repo.devcord.club/snapshots")
}

dependencies {
    implementation("edu.kit:ipasir4j:0.1.0-SNAPSHOT")
    implementation(project(":satviz-common"))
    implementation(project(":satviz-network"))
    implementation(project(":satviz-parsers"))
}

tasks {
    getByName<JavaCompile>("compileTestJava") {
        options.compilerArgs = listOf("--add-modules", "jdk.incubator.foreign")
    }

    getByName<Test>("test") {
        jvmArgs = listOf("--add-modules", "jdk.incubator.foreign", "--enable-native-access=ALL-UNNAMED")
    }
}
