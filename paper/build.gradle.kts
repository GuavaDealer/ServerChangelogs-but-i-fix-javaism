plugins {
    id("java")
    alias(libs.plugins.lombok)
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":base"))
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

paperPluginYaml {
    main = "${group}.changelogs.paper.PaperChangelogsPlugin"
    apiVersion = project.property("minecraft_version_compat").toString()

    authors.addAll("codingcat2468")
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
