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

jlink {
    imageName.set("satviz")
    launcher {
        name = "satviz"
        jvmArgs.addAll(listOf("--enable-native-access=edu.kit.satviz.consumer"))
    }
}

application {
    mainModule.set("edu.kit.satviz.consumer")
    mainClass.set("edu.kit.satviz.consumer.ConsumerApplication")
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
        commandLine = listOf("cmake", "../../..", "-DCMAKE_BUILD_TYPE=Debug")
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
        from(project(":satviz-producer").tasks.getByName("jlinkZip").outputs.files.singleFile)
    }

    test {
        dependsOn.add("ctest")
        jvmArgs = listOf("--add-modules", "jdk.incubator.foreign",
            "--enable-native-access=edu.kit.satviz.consumer")
    }

    compileTestJava {
        options.compilerArgs = listOf("--add-modules", "jdk.incubator.foreign")
    }
}
