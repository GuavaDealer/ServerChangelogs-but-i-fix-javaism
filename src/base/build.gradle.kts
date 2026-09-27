plugins {
    `java-library`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.platformapi)
    api(libs.slf4k)
    api(libs.kotaml)
    api(libs.kotlinx.serialization.core)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.packetevents.api)
    testImplementation(libs.kotlin.test)
}
