plugins {
    id("java")
}

dependencies {
    // Core Shared Kernel
    implementation(project(":taskspree-common"))

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)

    // AWS S3 SDK (works with MinIO)
    implementation(libs.aws.s3)

    // Spring Retry for upload retry logic
    implementation(libs.spring.retry)
    implementation(libs.spring.aspects)

    // Database
    runtimeOnly(libs.postgresql)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
}
