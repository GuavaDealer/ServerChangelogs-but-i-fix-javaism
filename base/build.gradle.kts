plugins {
    id("java-library")
    id("io.freefair.lombok") version "9.5.0"
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://libraries.minecraft.net")
}

dependencies {
    compileOnly("com.mojang:brigadier:1.3.10")
    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("net.kyori:adventure-text-logger-slf4j:4.26.1")
    compileOnly("com.github.retrooper:packetevents-api:2.13.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}