import java.io.File

repositories {
    mavenCentral()
}

plugins {
    application
    java
    id("com.google.osdetector") version "1.6.0"
    id("org.jetbrains.intellij") version "0.3.12"
}

application {
    mainClassName = "main.SokobanApplication"
}

java {
    // TODO(Derppening): Test with Java 10 before submitting!
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

intellij {
    instrumentCode = true
}

val platform: String = when (osdetector.os) {
    "osx" -> "mac"
    "windows" -> "win"
    else -> osdetector.os
}

val javaHome = System.getenv()["JAVA_HOME"] ?: ""

dependencies {
    val junit = "5.3.1"
    val junitPlatform = "1.3.1"
    val testfx = "4.0.15-alpha"

    // TODO(Derppening): Uncomment these when testing with Java 10
//    compile("org.openjfx:javafx-base:11:$platform")
//    compile("org.openjfx:javafx-controls:11:$platform")
//    compile("org.openjfx:javafx-graphics:11:$platform")
//    compile("org.openjfx:javafx-media:11:$platform")

    compile("org.jetbrains:annotations:16.0.3")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junit")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junit")
    testCompile("org.junit.platform:junit-platform-runner:$junitPlatform")
    testCompile("org.testfx:testfx-core:$testfx")
    testCompile("org.testfx:testfx-junit5:$testfx")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit")
    testRuntime("org.junit.platform:junit-platform-console:$junitPlatform")
}

sourceSets {
    getByName("test") {
        resources {
            srcDirs.add(File("src/main/resources"))
        }
    }
}

tasks {
    getByName<JavaCompile>("compileJava") {
        // TODO(Derppening): Uncomment these when testing with Java 10
        doFirst {
//            options.compilerArgs.addAll(listOf(
//                    "--module-path", classpath.asPath,
//                    "--add-modules", "javafx.controls,javafx.graphics,javafx.media"))
        }
    }

    getByName<JavaExec>("run") {
        // TODO(Derppening): Uncomment these when testing with Java 10
//        jvmArgs?.addAll(listOf(
//                "--module-path", classpath.asPath,
//                "--add-modules", "javafx.controls,javafx.graphics,javafx.media"))
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }

    getByName<Wrapper>("wrapper") {
        gradleVersion = "4.10.2"
        distributionType = Wrapper.DistributionType.ALL
    }

    register("fatJar", Jar::class) {
        dependsOn("jar")

        manifest {
            attributes.apply {
                this["Implementation-Title"] = "PA2"
                this["Main-Class"] = application.mainClassName
            }
        }

        baseName = "${project.name}-all"
        from(configurations.compile.map { if (it.isDirectory) it else zipTree(it) })
        with(tasks["jar"] as CopySpec)
    }
}
