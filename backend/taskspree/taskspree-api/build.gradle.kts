plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    // 1. Internal Modules
    implementation(project(":taskspree-common"))
    implementation(project(":taskspree-modules:taskspree-users"))

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
    implementation(libs.springdoc.openapi)

    // 5. Testing
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}