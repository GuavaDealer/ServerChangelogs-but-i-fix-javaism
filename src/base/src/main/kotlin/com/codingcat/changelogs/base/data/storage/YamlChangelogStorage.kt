package com.codingcat.changelogs.base.data.storage

import com.codingcat.changelogs.base.data.ChangelogEntry
import com.codingcat.changelogs.base.data.ChangelogStorage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Suppress("UNCHECKED_CAST")
class YamlChangelogStorage(
    private val filePath: Path,
) : ChangelogStorage {
    private val lock = Any()
    private var cache: MutableList<ChangelogEntry> = mutableListOf()

    override val displayName: String = "YAML File"

    override fun init() {
        synchronized(lock) {
            if (!filePath.exists()) {
                this.cache = mutableListOf()
                this.saveLocked()
                return
            }
            try {
                val config = Yaml()
                val data = filePath.inputStream().use { stream ->
                    config.load<Map<String, Any?>>(stream)
                }
                this.cache = this.deserializeEntries(data ?: emptyMap())
            } catch (e: IOException) {
                throw RuntimeException("Failed to load changelog data from ${filePath}", e)
            } catch (e: YAMLException) {
                throw RuntimeException("Failed to load changelog data from ${filePath}", e)
            }
        }
    }

    override fun shutdown() {
    }

    override fun storeEntry(entry: ChangelogEntry) {
        synchronized(lock) {
            this.cache.removeIf { it.uid == entry.uid }
            this.cache.add(entry)
            this.saveLocked()
        }
    }

    override fun updateEntry(entry: ChangelogEntry) {
        synchronized(lock) {
            val idx = this.cache.indexOfFirst { it.uid == entry.uid }
            if (idx >= 0) {
                this.cache[idx] = entry
            } else {
                this.cache.add(entry)
            }
            this.saveLocked()
        }
    }

    override fun removeEntry(uid: Int): Boolean {
        synchronized(lock) {
            val success = this.cache.removeIf { it.uid == uid }
            if (success) {
                this.saveLocked()
            }
            return success
        }
    }

    private fun saveLocked() {
        val config = Yaml()
        val data = this.cache.map { serializeEntry(it) }
        val tempPath = filePath.resolveSibling("${filePath.fileName}.tmp")
        try {
            tempPath.outputStream().use { os ->
                config.dump(
                    mapOf("entries" to data),
                    os.writer().buffered(),
                )
            }
            try {
                Files.move(
                    tempPath,
                    filePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to save changelog data to ${filePath}", e)
        }
    }

    private fun deserializeEntries(config: Map<String, Any?>): MutableList<ChangelogEntry> {
        return runCatching {
            val entries = config["entries"] as? List<Map<String, Any?>> ?: emptyList()
            entries.mapIndexedTo(mutableListOf()) { index, entryMap ->
                deserializeEntry(index, entryMap)
            }
        }
            .getOrElse { e ->
                throw RuntimeException("Failed to deserialize changelog entries", e)
            }
    }

    private fun deserializeEntry(defaultIndex: Int, data: Map<String, Any?>): ChangelogEntry {
        val uid = (data["uid"] as? Number)?.toInt() ?: defaultIndex
        val linesRaw = data["serializedLines"] as? List<String> ?: emptyList()
        val lines = linesRaw.map { GsonComponentSerializer.gson().deserialize(it) }
        val recordedAtRaw = data["recordedAt"] as? String
            ?: throw IllegalStateException("Missing recordedAt for changelog entry ${uid}")
        val recordedAt = DateTimeFormatter.ISO_INSTANT.parse(recordedAtRaw, Instant::from)
        val author = (data["author"] as? String)?.let { GsonComponentSerializer.gson().deserialize(it) }
        val playersReadRaw = data["playersRead"] as? List<String> ?: emptyList()
        val playersRead = playersReadRaw.mapTo(mutableSetOf()) { UUID.fromString(it) }
        return ChangelogEntry(uid, lines, recordedAt, author, playersRead)
    }

    private fun serializeEntry(entry: ChangelogEntry): Map<String, Any?> {
        val serializedLines = entry.lines.map { GsonComponentSerializer.gson().serialize(it) }
        val recordedAtRaw = DateTimeFormatter.ISO_INSTANT.format(entry.recordedAt)
        val serializedAuthor = entry.author?.let { GsonComponentSerializer.gson().serialize(it) }
        val rawPlayersRead = entry.playersRead.map { it.toString() }
        return mapOf(
            "uid" to entry.uid,
            "serializedLines" to serializedLines,
            "recordedAt" to recordedAtRaw,
            "author" to serializedAuthor,
            "playersRead" to rawPlayersRead,
        )
    }

    override fun listEntries(): List<ChangelogEntry> {
        synchronized(lock) {
            return this.cache.toList()
        }
    }

    override fun getByUID(uid: Int): ChangelogEntry? {
        synchronized(lock) {
            return this.cache.find { it.uid == uid }
        }
    }

    override fun markAsRead(uid: Int, player: UUID) {
        synchronized(lock) {
            val entry = this.cache.find { it.uid == uid } ?: return
            if (entry.playersRead.add(player)) {
                this.saveLocked()
            }
        }
    }

    override fun nextUID(): Int {
        synchronized(lock) {
            return (this.cache.maxOfOrNull { it.uid } ?: -1) + 1
        }
    }
}
