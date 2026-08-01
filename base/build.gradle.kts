plugins {
    id("java-library")
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(project(":platformapi"))
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.slf4j)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.packetevents.api)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}