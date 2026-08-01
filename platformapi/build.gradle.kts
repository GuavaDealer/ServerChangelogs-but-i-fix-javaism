plugins {
    id("java-library")
    alias(libs.plugins.lombok)
}

dependencies {
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.slf4j)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.slf4j.api)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}