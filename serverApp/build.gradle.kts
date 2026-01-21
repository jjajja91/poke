import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springboot)
    alias(libs.plugins.springDependencyManagement)
}

group = "poke"
version = "1.0.0"

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") { enabled = true }
tasks.named<Jar>("jar") { enabled = false }

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
    // 사용 JDK는 25지만, 타깃 릴리스는 24
    options.release.set(24)
}


dependencies {
    api(libs.spring.boot.starter.web)
    api(libs.spring.boot.starter.validation)
    api(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.flyway.postgresql)
    runtimeOnly(libs.flyway.mysql)
    runtimeOnly(libs.postgresql.driver)
    runtimeOnly(libs.mysql.connector.j)
    api(libs.flyway.core)
    implementation(project(":serverPokemon"))
}
