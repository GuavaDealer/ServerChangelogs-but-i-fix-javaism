package com.codingcat.changelogs.base.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.codingcat.changelogs.base.ServerChangelogs
import com.codingcat.changelogs.base.compat.PacketEventsFix
import com.codingcat.changelogs.base.data.ChangelogStorage
import com.codingcat.changelogs.base.dialog.DialogPackets
import com.github.retrooper.packetevents.protocol.item.ItemStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import java.nio.file.Path
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.readText

/**
 * Serialized configuration model for plugin settings.
 */
@Serializable
data class PluginConfigData(
    @SerialName("changelog_storage")
    val changelogStorage: String = "yaml",
    @SerialName("date_format")
    val dateFormat: String = "MM/dd/yyyy",
    @SerialName("date_timezone")
    val dateTimezone: String = "UTC",
    @SerialName("register_dedicated_command")
    val registerDedicatedCommand: Boolean = true,
    @SerialName("use_native_fallback_permissions")
    val useNativeFallbackPermissions: Boolean = false,
    @SerialName("dialog_phase")
    val dialogPhase: String = "CONFIGURATION",
    @SerialName("dialog_header")
    val dialogHeader: Boolean = true,
    @SerialName("dialog_header_item")
    val dialogHeaderItem: String? = "minecraft:written_book",
    @SerialName("enable_manual_workarounds")
    val enableManualWorkarounds: List<String> = emptyList(),
)

/**
 * YAML configuration parser and validator managing plugin behavior, item stacks, and storage options.
 */
class PluginConfig(
    private val path: Path,
) {
    private val yaml: Yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
    private var data: PluginConfigData = PluginConfigData()

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
            throw RuntimeException("Configuration invalid: ${err}")
        }
    }

    /**
     * Asynchronously attempts to reload and validate configuration on Dispatchers.IO.
     */
    suspend fun tryReloadAsync() = withContext(Dispatchers.IO) {
        tryReload()
    }

    /**
     * Asynchronously reloads configuration from disk on Dispatchers.IO.
     */
    suspend fun reloadAsync() = withContext(Dispatchers.IO) {
        reload()
    }

    fun reload() {
        val text = this.path.readText()
        this.data = if (text.isBlank()) {
            PluginConfigData()
        } else {
            this.yaml.decodeFromString(PluginConfigData.serializer(), text)
        }
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
        return ChangelogStorage.create(data.changelogStorage)
    }

    val dateFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(data.dateFormat)
            .withZone(ZoneId.of(data.dateTimezone))

    fun registerDedicatedCommand(): Boolean {
        return data.registerDedicatedCommand
    }

    val dialogPacketPhase: DialogPackets.PacketPhase
        get() {
            val rawPhase = data.dialogPhase
            return runCatching {
                DialogPackets.PacketPhase.valueOf(rawPhase.uppercase())
            }.getOrElse {
                throw IllegalArgumentException("Invalid dialog phase \"${rawPhase}\"")
            }
        }

    val enabledManualWorkarounds: Set<PacketEventsFix.Workaround>
        get() {
            return data.enableManualWorkarounds.map { workaround ->
                runCatching {
                    PacketEventsFix.Workaround.valueOf(workaround.uppercase())
                }.getOrElse {
                    throw IllegalArgumentException("Invalid manual workaround ID \"${workaround}\"")
                }
            }.toSet()
        }

    fun showChangelogHeader(): Boolean {
        return data.dialogHeader
    }

    fun useNativeFallbackPermissions(): Boolean {
        return data.useNativeFallbackPermissions
    }

    fun createChangelogHeaderStack(): ItemStack? =
        data.dialogHeaderItem?.let(::createStack)

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
