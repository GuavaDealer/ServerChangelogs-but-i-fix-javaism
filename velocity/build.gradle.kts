import xyz.jpenilla.resourcefactory.velocity.VelocityPluginJson

plugins {
    id("java")
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
    id("xyz.jpenilla.run-velocity") version "3.0.2"
    id("xyz.jpenilla.resource-factory-velocity-convention") version "1.3.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":base"))
    implementation(project(":platformapi"))
    compileOnly(libs.packetevents.velocity)
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
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
        // Configure the Velocity version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        velocityVersion("3.5.0-SNAPSHOT")
    }
}
