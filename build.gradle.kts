plugins {
    java
    checkstyle
}

subprojects {
    group = "edu.kit.satviz"

    apply(plugin = "java")
    apply(plugin = "checkstyle")

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.mockito:mockito-core:3.+")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    checkstyle {
        configFile = configDirectory.file("google_checks.xml").get().asFile
    }

}