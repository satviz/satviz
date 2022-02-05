plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.24.4"
}

dependencies {
    implementation(project(":satviz-network"))
    implementation(project(":satviz-serial"))
    implementation(project(":satviz-sat"))
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
    implementation(project(":satviz-parsers"))
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}

val nativeModuleDir = rootProject.file("satviz-consumer-native")
val nativeBuildDir = nativeModuleDir.resolve("build")
val sharedLibFile = nativeBuildDir.resolve("src/satviz/libsatviz-consumer-native.so")

tasks {
    register<Exec>("cmake") {
        group = "native"
        outputs.dir(nativeBuildDir)
        doFirst {
            nativeBuildDir.mkdir()
        }
        workingDir = nativeBuildDir
        commandLine = listOf("cmake", "..")
    }

    register<Exec>("make") {
        group = "native"
        dependsOn.add("cmake")
        workingDir = nativeBuildDir
        commandLine = listOf("make")
    }

    processResources {
        dependsOn.add("make")
        from(sharedLibFile)
    }

    clean {
        delete.add(nativeBuildDir)
        delete.add(nativeModuleDir.resolve("cmake-build-debug"))
    }

}