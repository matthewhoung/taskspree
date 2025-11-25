plugins {
    id("java")
}

dependencies {
    // 1. Allow access to 'Entity', 'Result', 'Error' classes
    implementation(project(":taskspree-common"))

    // 2. Allow access to JPA Annotations (@Entity, @Table, @Column)
    implementation(libs.spring.boot.starter.data.jpa)

    // 3. Validation (@NotNull, etc.)
    implementation(libs.spring.boot.starter.validation)

    // 4. Testing
    testImplementation(libs.spring.boot.starter.test)
}