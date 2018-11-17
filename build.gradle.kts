import java.io.File

repositories {
    mavenCentral()
}

plugins {
    application
    java
}

application {
    mainClassName = "main.SokobanApplication"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    val junit = "5.3.1"
    val junitPlatform = "1.3.1"
    val testfx = "4.0.15-alpha"

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
