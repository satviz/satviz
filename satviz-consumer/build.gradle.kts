plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.24.4"
}

dependencies {
    implementation(project(":satviz-network"))
    implementation(project(":satviz-serial"))
    implementation(project(":satviz-sat"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
    implementation(project(":satviz-parsers"))
}

javafx {
    modules("javafx.controls", "javafx.fxml")
    version = "17"
}

val nativeBuildDir = project.buildDir.resolve("cmake-build")
val consumerLib = nativeBuildDir.resolve("satviz-consumer-native/src/satviz/libsatviz-consumer-native.so")
val ogdfBuild = rootProject.projectDir.resolve(".ogdf-build")
val ogdfLib = ogdfBuild.resolve("libOGDF.so")
val coinLib = ogdfBuild.resolve("libCOIN.so")

tasks {
    register<Exec>("cmake") {
        group = "native"
        inputs.dir("../satviz-consumer-native")
        outputs.dir(nativeBuildDir.absoluteFile)
        outputs.dir(ogdfBuild.absoluteFile)
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