package com.codingcat.changelogs.base.dialog.ui.editor

import com.codingcat.changelogs.base.data.ChangelogEntry
import com.codingcat.changelogs.base.data.ChangelogStorage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.time.Instant

sealed class EditorSession(
    val entryUID: Int,
) {
    val rawLines: MutableList<String> = mutableListOf()
    var editingLineIndex: Int = -1
    var currentLine: String = ""
    var author: String = ""
    var showRestoredMessage: Boolean = false

    @Throws(CommitException::class)
    abstract fun commit(storage: ChangelogStorage)

    abstract val id: String

    abstract val permission: String

    abstract fun canBeSaved(): Boolean

    fun deserializeLines(): List<Component> {
        return rawLines.map { MiniMessage.miniMessage().deserialize(it) }
    }

    fun deserializeAuthor(): Component? {
        return if (author.isNotBlank()) MiniMessage.miniMessage().deserialize(author) else null
    }

    class Create(entryUID: Int) : EditorSession(entryUID) {
        override val id: String = "create"
        override val permission: String = "command.create"

        override fun canBeSaved(): Boolean = rawLines.isNotEmpty() || author.isNotBlank()

        @Throws(CommitException::class)
        override fun commit(storage: ChangelogStorage) {
            val newEntry = ChangelogEntry(
                entryUID,
                this.deserializeLines(),
                Instant.now(),
                this.deserializeAuthor(),
                mutableSetOf(),
            )
            runCatching {
                storage.storeEntry(newEntry)
            }.getOrElse {
                throw CommitException("internal_error")
            }
        }
    }

    class Edit(entry: ChangelogEntry) : EditorSession(entry.uid) {
        init {
            entry.lines.mapTo(rawLines) { MiniMessage.miniMessage().serialize(it) }
            this.author = entry.author?.let { MiniMessage.miniMessage().serialize(it) } ?: ""
        }

        override val id: String = "edit"
        override val permission: String = "manage"

        override fun canBeSaved(): Boolean = false

        @Throws(CommitException::class)
        override fun commit(storage: ChangelogStorage) {
            val currentEntry: ChangelogEntry = storage.getByUID(entryUID)
                ?: throw CommitException("entry_deleted")
            val newEntry = ChangelogEntry(
                currentEntry.uid,
                this.deserializeLines(),
                currentEntry.recordedAt,
                this.deserializeAuthor(),
                currentEntry.playersRead,
            )
            runCatching {
                storage.updateEntry(newEntry)
            }.getOrElse {
                throw CommitException("internal_error")
            }
        }
    }

    class CommitException(val translationKeyPart: String) : Exception()
}
