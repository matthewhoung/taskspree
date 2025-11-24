plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    // 1. Internal Modules
    implementation(project(":taskspree-common"))
    // implementation(project(":taskspree-modules:taskspree-users")) // Keep commented until we create the users module

    // 2. Core Spring
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)

    // 3. Architecture (Modulith)
    implementation(libs.spring.modulith.starter.core)
    implementation(libs.spring.modulith.starter.jpa)

    // 4. Database & Migration
    runtimeOnly(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    // 5. Documentation
    implementation(libs.springdoc.openapi)

    // 6. Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.modulith.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}