plugins {
    `java-library`
}

dependencies {
    api(libs.kotlinx.immutable)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.packetevents.api)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.slf4j.api)
    testImplementation(libs.kotlin.test)
}
