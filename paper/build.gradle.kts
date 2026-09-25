plugins {
    java
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory.paper.convention)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    implementation(project(":base"))
    implementation(project(":platformapi"))
    compileOnly(libs.packetevents.paper)
    compileOnly(libs.paper.api)
}

paperPluginYaml {
    name = rootProject.name
    main = "${group}.changelogs.paper.PaperChangelogsPlatform"
    website = project.property("url") as String
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
        minecraftVersion(project.property("minecraft_version").toString())
        jvmArgs("-Xms1G", "-Xmx1G")
    }
}
