plugins {
    id("java")
}

dependencies {
    // 1. JPA is required here because 'Entity.java' uses @MappedSuperclass
    implementation(libs.spring.boot.starter.data.jpa)

    // 2. Validation is useful here for common value objects (Result/Error)
    implementation(libs.spring.boot.starter.validation)

    // 3. Test support for common utilities
    testImplementation(libs.spring.boot.starter.test)
}