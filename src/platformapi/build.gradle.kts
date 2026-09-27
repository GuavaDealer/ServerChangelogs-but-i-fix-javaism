plugins {
    `java-library`
}

dependencies {
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.coroutines.core)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.packetevents.api)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.slf4j.api)
    testImplementation(libs.kotlin.test)
}
