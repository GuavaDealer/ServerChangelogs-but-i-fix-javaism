package com.codingcat.changelogs.base.data

import com.codingcat.changelogs.base.ServerChangelogs
import com.codingcat.changelogs.base.data.storage.YamlChangelogStorage
import java.util.*

/**
 * Persistence abstraction for storing, updating, reading, and indexing [ChangelogEntry] items.
 */
interface ChangelogStorage : Iterable<ChangelogEntry> {
    /**
     * Initializes storage structures, loading records into memory.
     */
    fun init()

    /**
     * Flushes modified records to persistent storage and closes resources.
     */
    fun shutdown()

    /**
     * Persists a new changelog [entry].
     */
    fun storeEntry(entry: ChangelogEntry)

    /**
     * Updates an existing changelog [entry] matching its unique ID.
     */
    fun updateEntry(entry: ChangelogEntry)

    /**
     * Deletes the changelog entry identified by [uid]. Returns true if an entry was removed.
     */
    fun removeEntry(uid: Int): Boolean

    /**
     * Returns an ordered snapshot list of all stored changelog entries.
     */
    fun listEntries(): List<ChangelogEntry>

    /**
     * Finds a changelog entry by its [uid], returning `null` if not found.
     */
    fun getByUID(uid: Int): ChangelogEntry?

    /**
     * Marks the entry with [uid] as acknowledged/read by [player].
     */
    fun markAsRead(uid: Int, player: UUID)

    /**
     * Computes the next available unique identifier for a new entry.
     */
    fun nextUID(): Int

    /**
     * User-facing display name of this storage engine.
     */
    val displayName: String

    override fun iterator(): Iterator<ChangelogEntry> = listEntries().iterator()

    /**
     * Index operator lookup by [uid].
     */
    operator fun get(uid: Int): ChangelogEntry? = getByUID(uid)

    /**
     * Checks if an entry with [uid] exists in storage.
     */
    operator fun contains(uid: Int): Boolean = getByUID(uid) != null

    /**
     * Appends an [entry] using `+=` operator syntax.
     */
    operator fun plusAssign(entry: ChangelogEntry) {
        storeEntry(entry)
    }

    companion object {
        /**
         * Resolves and instantiates a [ChangelogStorage] backend for the requested [identifier].
         *
         * @throws IllegalArgumentException if the storage type identifier is unsupported.
         */
        @Throws(IllegalArgumentException::class)
        fun create(identifier: String): ChangelogStorage {
            require(identifier == "yaml") { "Unknown changelog storage type \"${identifier}\"" }
            return YamlChangelogStorage(ServerChangelogs.platform.getDataPath().resolve("_data.yml"))
        }
    }
}
