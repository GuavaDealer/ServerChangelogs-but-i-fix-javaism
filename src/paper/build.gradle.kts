plugins {
    id("com.gradleup.shadow")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory.paper.convention)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    implementation(projects.base)
    implementation(projects.platformapi)
    implementation(libs.mccoroutine.bukkit.api)
    implementation(libs.mccoroutine.bukkit.core)
    compileOnly(libs.packetevents.paper)
    compileOnly(libs.paper.api)
    testImplementation(libs.kotlin.test)
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

tasks {
    runServer {
        minecraftVersion(project.property("minecraft_version").toString())
        jvmArgs("-Xms1G", "-Xmx1G")
    }
}
