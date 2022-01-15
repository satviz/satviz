plugins {
    `java-library`
    id("org.openjfx.javafxplugin") version "0.0.10"
}

dependencies {
    api(project(":satviz-config"))
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}
