dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "poke"


include(":serverApp")
include(":common")
include(":serverType")
include(":serverVersion")
include(":serverAbility")
include(":serverMove")
include(":serverPokemon")