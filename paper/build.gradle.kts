plugins {
    id("java")
    alias(libs.plugins.lombok)
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":base"))
    implementation(project(":platformapi"))
    compileOnly(libs.packetevents.paper)
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

paperPluginYaml {
    name = rootProject.name
    main = "${group}.changelogs.paper.PaperChangelogsPlatform"
    apiVersion = project.property("minecraft_version_compat").toString()
    foliaSupported = true

    authors.addAll("codingcat2468")
    dependencies.server.create("packetevents")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion(project.property("minecraft_version").toString())
        jvmArgs("-Xms1G", "-Xmx1G")
    }
}
