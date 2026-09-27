plugins {
    `java-library`
}

dependencies {
    implementation(projects.platformapi)
    api(libs.slf4k)
    compileOnly(libs.snakeyaml)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.mojang.brigadier)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.packetevents.api)
    testImplementation(libs.kotlin.test)
}
