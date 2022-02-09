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
    testImplementation("org.mockito:mockito-inline:4.3.1")
    testImplementation("org.mockito:mockito-junit-jupiter:4.3.1")
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}
