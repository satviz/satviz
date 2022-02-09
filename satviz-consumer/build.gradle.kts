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

val nativeBuildDir = project.buildDir.resolve("cmake-build")
val consumerLib = nativeBuildDir.resolve("satviz-consumer-native/src/satviz/libsatviz-consumer-native.so")
val ogdfLib = rootProject.projectDir.resolve(".ogdf-build/libOGDF.so")
val coinLib = rootProject.projectDir.resolve(".ogdf-build/libCOIN.so")

tasks {
    register<Exec>("cmake") {
        group = "native"
        outputs.dir(nativeBuildDir)
        doFirst {
            nativeBuildDir.mkdir()
        }
        workingDir = nativeBuildDir
        commandLine = listOf("cmake", "../../..")
    }

    register<Exec>("ctest") {
        group = "native"
        dependsOn.add("cmake")
        workingDir = nativeBuildDir
        commandLine = listOf("ctest")
    }

    register<Exec>("make") {
        group = "native"
        dependsOn.add("cmake")
        workingDir = nativeBuildDir
        commandLine = listOf("make", "-j", "6")
    }

    processResources {
        dependsOn.add("make")
        from(consumerLib)
        from(ogdfLib)
        from(coinLib)
    }

    test {
        dependsOn.add("ctest")
    }
}