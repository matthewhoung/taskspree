plugins {
    alias(libs.plugins.spring.boot)
}

import org.springframework.boot.gradle.tasks.run.BootRun

dependencies {
    // 1. Internal Modules
    implementation(project(":taskspree-common"))
    implementation(project(":taskspree-modules:taskspree-users"))
    implementation(project(":taskspree-modules:taskspree-identity"))

    // 2. Core Spring
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)

    // 3. Database & Migration
    runtimeOnly(libs.postgresql)
    implementation(libs.spring.boot.starter.flyway)
    runtimeOnly(libs.flyway.database.postgresql)

    // 4. Documentation
    implementation(libs.dotenv)
    implementation(libs.springdoc.openapi)

    // 5. Testing
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<BootRun>("bootRun") {
    // 1. Point to the .env file in the root project folder
    val envFile = rootProject.file(".env")

    // 2. Check if it exists and load variables
    if (envFile.exists()) {
        envFile.readLines().forEach { line ->
            // Skip comments and empty lines
            if (line.isNotBlank() && !line.trim().startsWith("#")) {
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    // Inject into the Java process
                    environment(key, value)
                }
            }
        }
        println("Loaded environment variables from: ${envFile.name}")
    } else {
        println("WARNING: .env file not found at ${envFile.absolutePath}")
    }
}