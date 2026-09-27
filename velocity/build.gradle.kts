import xyz.jpenilla.resourcefactory.velocity.VelocityPluginJson

plugins {
    id("java")
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.velocity)
    alias(libs.plugins.resource.factory.velocity.convention)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    implementation(project(":base"))
    implementation(project(":platformapi"))
    compileOnly(libs.packetevents.velocity)
    compileOnly(libs.velocity.api)
}

velocityPluginJson {
    id = "server-changelogs"
    name = rootProject.name
    url = project.property("url") as String

    main = "${group}.changelogs.velocity.VelocityChangelogsPlatform"
    authors.addAll("codingcat2468")
    dependencies.addAll(VelocityPluginJson.Dependency("packetevents", false))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runVelocity {
        velocityVersion(libs.versions.velocity.api.get())
    }
}
