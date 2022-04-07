import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.24.4"
}

dependencies {
    implementation(project(":satviz-network"))
    implementation(project(":satviz-serial"))
    implementation(project(":satviz-sat"))
    implementation(project(":satviz-common"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
    implementation("net.lingala.zip4j:zip4j:2.9.1")
    implementation(project(":satviz-parsers"))
    implementation(project(":satviz-common"))
}

javafx {
    modules("javafx.controls", "javafx.fxml")
    version = "17"
}

jlink {
    launcher {
        name = "satviz"
        jvmArgs = listOf("--enable-native-access=edu.kit.satviz.consumer")
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


    val jlinkTask = getByName<org.beryx.jlink.JlinkTask>("jlink")

    fun installTask(name: String, dir: String) =
        register<Copy>(name) {
            val installDir = rootProject.projectDir.resolve(dir)
            doFirst {
                installDir.deleteRecursively()
            }
            dependsOn.add(jlinkTask)
            from(jlinkTask.imageDir)
            into(installDir)
        }


    installTask("installTestBuild", "test-run/satviz")

    val installationDir = "/opt/satviz/"
    val installation = installTask("install", installationDir).configure {
        finalizedBy("createGlobalScript")
    }

    register<DefaultTask>("createGlobalScript") {
        doLast {
            val file = file("/usr/bin/satviz")
            if (!file.exists()) {
                Files.createFile(
                    file.toPath(),
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"))
                )
            }
            file.writeText("exec ${installationDir}bin/satviz $@\n")
        }
    }

    register<Zip>("satvizDist") {
        dependsOn.add(jlinkTask)
        from(jlinkTask.imageDir)
        destinationDirectory.set(buildDir)
        archiveBaseName.set("satviz")
    }

    processResources {
        val producerBuild = project(":satviz-producer").tasks.getByName("jlinkZip")
        dependsOn.add(producerBuild)
        dependsOn.add("make")
        from(consumerLib)
        from(ogdfLib)
        from(coinLib)
        from(producerBuild.outputs.files.singleFile)
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
