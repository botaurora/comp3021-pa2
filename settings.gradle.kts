rootProject.name = "PA2"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.osdetector") {
                useModule("com.google.gradle:osdetector-gradle-plugin:${requested.version}")
            }
        }
    }
}
