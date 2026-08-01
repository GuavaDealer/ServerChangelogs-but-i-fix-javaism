plugins {
    id("java-library")
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(project(":platformapi"))
    // Since SnakeYAML is included in some platforms, it is included as compileOnly here
    // The library can be declared as implementation and shadowed on platforms where it is not natively included
    compileOnly(libs.snakeyaml)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.slf4j)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.adventure.gson)
    compileOnly(libs.packetevents.api)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}