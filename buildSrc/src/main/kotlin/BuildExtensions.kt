import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

fun Project.java(configure: Action<JavaPluginExtension>): Unit =
    extensions.configure("java", configure)

fun Project.kotlin(configure: Action<KotlinJvmProjectExtension>): Unit =
    extensions.configure("kotlin", configure)

fun Project.shadowJar(configure: Action<ShadowJar>) {
    pluginManager.withPlugin("com.gradleup.shadow") {
        tasks.named("shadowJar", ShadowJar::class.java, configure)
    }
}

fun TaskContainer.shadowJar(configure: Action<ShadowJar>): TaskProvider<ShadowJar> =
    named("shadowJar", ShadowJar::class.java, configure)

fun Project.missingProperty(name: String): Nothing =
    throw IllegalStateException("Property '${name}' is missing. Please define it in gradle.properties.")

fun missingProperty(name: String): Nothing =
    throw IllegalStateException("Property '${name}' is missing. Please define it in gradle.properties.")
