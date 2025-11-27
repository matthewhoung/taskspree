plugins {
    id("java")
}

dependencies {
    // Core Shared Kernel
    implementation(project(":taskspree-common"))

    // One-way dependency: Identity -> Users (for SignUp orchestration)
    implementation(project(":taskspree-modules:taskspree-users"))

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)

    // Third Party
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    runtimeOnly(libs.postgresql)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
}