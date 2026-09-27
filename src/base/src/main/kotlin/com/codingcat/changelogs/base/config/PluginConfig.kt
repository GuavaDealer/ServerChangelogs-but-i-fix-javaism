package com.codingcat.changelogs.base.config

import com.codingcat.changelogs.base.ServerChangelogs
import com.codingcat.changelogs.base.compat.PacketEventsFix
import com.codingcat.changelogs.base.data.ChangelogStorage
import com.codingcat.changelogs.base.dialog.DialogPackets
import com.github.retrooper.packetevents.protocol.item.ItemStack
import net.kyori.adventure.key.Key
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import java.io.IOException
import java.nio.file.Path
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.inputStream

/**
 * YAML configuration parser and validator managing plugin behavior, item stacks, and storage options.
 */
@Suppress("UNCHECKED_CAST")
class PluginConfig(
    private val path: Path,
) {
    private val yaml: Yaml = Yaml()
    private var data: Map<String, Any?> = emptyMap()

    /**
     * Attempts to reload and validate the configuration file, throwing [RuntimeException] on failure.
     */
    fun tryReload() {
        runCatching {
            this.reload()
        }.onFailure { e ->
            throw RuntimeException("Failed to reload configuration", e)
        }
        this.validate()?.let { err ->
            throw RuntimeException("Configuration invalid: $err")
        }
    }

    @Throws(IOException::class, YAMLException::class)
    fun reload() {
        this.data = this.path.inputStream().use { stream ->
            this.yaml.load(stream)
        } ?: emptyMap()
    }

    fun validate(): String? = runCatching {
        this.dialogPacketPhase
        createChangelogStorage()
        this.dateFormatter
        createChangelogHeaderStack()
        this.enabledManualWorkarounds
        null
    }.getOrElse { it.message }

    @Throws(RuntimeException::class)
    fun createChangelogStorage(): ChangelogStorage {
        return ChangelogStorage.create(getString("changelog_storage", "yaml"))
    }

    val dateFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(getString("date_format", "----"))
            .withZone(ZoneId.of(getString("date_timezone", "UTC")))

    fun registerDedicatedCommand(): Boolean {
        return getBoolean("register_dedicated_command", true)
    }

    val dialogPacketPhase: DialogPackets.PacketPhase
        get() {
            val rawPhase = getString("dialog_phase", DialogPackets.PacketPhase.PLAY.name)
            return runCatching {
                DialogPackets.PacketPhase.valueOf(rawPhase.uppercase())
            }.getOrElse {
                throw IllegalArgumentException("Invalid dialog phase \"${rawPhase}\"")
            }
        }

    val enabledManualWorkarounds: Set<PacketEventsFix.Workaround>
        get() {
            val rawWorkarounds = getList<String>("enable_manual_workarounds")
            return rawWorkarounds.mapNotNull { workaround ->
                workaround?.let { it ->
                    runCatching {
                        PacketEventsFix.Workaround.valueOf(it.uppercase())
                    }.getOrElse {
                        throw IllegalArgumentException("Invalid manual workaround ID \"${it}\"")
                    }
                }
            }.toSet()
        }

    fun showChangelogHeader(): Boolean {
        return getBoolean("dialog_header", true)
    }

    fun useNativeFallbackPermissions(): Boolean {
        return getBoolean("use_native_fallback_permissions", false)
    }

    fun createChangelogHeaderStack(): ItemStack? =
        getStringOrNull("dialog_header_item")?.let(::createStack)

    private fun getString(key: String, defaultValue: String): String {
        return (this.data[key] as? String) ?: defaultValue
    }

    private fun getStringOrNull(key: String): String? {
        return this.data[key] as? String
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return (this.data[key] as? Boolean) ?: defaultValue
    }

    private inline fun <reified T> getList(key: String): List<T?> {
        val list = this.data[key] as? List<*> ?: return emptyList()
        return list.map { it as? T }
    }

    private fun createStack(input: String): ItemStack {
        val idPart = if (input.contains("[")) input.substring(0, input.indexOf('[')) else input
        val componentPart = if (input.contains("[")) input.substring(input.indexOf('[')) else null
        val inputKey = runCatching {
            Key.key(idPart)
        }.getOrElse { e ->
            throw RuntimeException("Invalid item ID \"${idPart}\"", e)
        }
        val nativeManager = ServerChangelogs.platform.nativeItemManager
        var nativeStack: Any = nativeManager.createNativeStack(inputKey, 1)
            ?: throw NullPointerException("Unknown item ID \"${idPart}\"")
        componentPart?.let {
            nativeStack = nativeManager.applyComponentStr(nativeStack, it)
        }
        return nativeManager.adaptToPEStack(nativeStack)
    }
}
