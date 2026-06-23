plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

// :domain is a pure Kotlin/JVM library with zero Android dependencies.
// It is the inward-pointing core of the dependency graph: everyone may depend
// on it, it depends on no other project module.
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// Keep Kotlin's JVM target aligned with Java (11) to avoid the cross-task mismatch.
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // Pure-JVM coroutines only — no Android, no Compose, no Hilt.
    implementation(libs.kotlinx.coroutines.core)
}
