import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springboot)
    alias(libs.plugins.springDependencyManagement)
}

group = "server.type"

tasks.named<Jar>("jar") { enabled = true }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


kotlin {
    jvmToolchain(25)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_24)
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xjvm-default=all",
            // "-Xcontext-parameters",
            // "-Xwhen-guards",
            // "-Xmulti-dollar-interpolation",
            // "-Xnon-local-break-continue",
            // "-Xcontext-sensitive-resolution",
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(24)
}

dependencies {
    api(project(":serverType"))
}