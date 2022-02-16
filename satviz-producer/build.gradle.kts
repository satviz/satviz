plugins {
    java
    id("org.beryx.jlink") version "2.24.4"
}

repositories {
    maven("https://repo.devcord.club/releases")
}

dependencies {
    implementation("edu.kit:ipasir4j:0.1.0")
    implementation("org.lz4:lz4-java:1.8.0")
    implementation(project(":satviz-common"))
    implementation(project(":satviz-network"))
    implementation(project(":satviz-parsers"))
}

application {
    mainModule.set("edu.kit.satviz.producer")
    mainClass.set("edu.kit.satviz.producer.ProducerApplication")
}

jlink {
    launcher {
        name = "sat-prod"
    }
}

tasks {

    compileTestJava {
        options.compilerArgs = listOf("--add-modules", "jdk.incubator.foreign")
    }

    test {
        jvmArgs = listOf("--add-modules", "jdk.incubator.foreign", "--enable-native-access=ALL-UNNAMED")
    }
}
