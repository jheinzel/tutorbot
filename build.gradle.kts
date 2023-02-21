plugins {
    val kotlinVersion = "1.3.70"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
}

group = "at.fhooe.hagenberg"
version = "1.4.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // CLI interface
    implementation("info.picocli:picocli:4.2.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.4.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.4.0")

    // HTML parsing
    implementation("org.jsoup:jsoup:1.13.1")

    // Dependency injection
    implementation("com.google.dagger:dagger:2.27")
    kapt("com.google.dagger:dagger-compiler:2.27")

    // Local JARs (JPlag)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // eails
    implementation("javax.mail:javax.mail-api:1.6.2")
    implementation("com.sun.mail:javax.mail:1.6.2")

    // Unit testing
    testImplementation("junit:junit:4.12")

    // Testing rules for CLI tools
    testImplementation("com.github.stefanbirkner:system-rules:1.19.0")

    // General mocking
    testImplementation("io.mockk:mockk:1.9.3")

    // Web request mocking
    testImplementation("com.squareup.okhttp3:mockwebserver:4.4.0")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    jar {
        archiveFileName.set("tutorbot.jar")
        from(configurations.runtimeClasspath.get().map { file -> if (file.isDirectory) file else zipTree(file) })
        manifest {
            attributes(
                "Main-Class" to "at.fhooe.hagenberg.tutorbot.Tutorbot",
                "Implementation-Version" to archiveVersion
            )
        }
    }
}
