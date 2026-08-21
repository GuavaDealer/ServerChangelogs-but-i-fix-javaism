package com.codingcat.changelogs.base.dialog.ui.editor;

import com.codingcat.changelogs.base.data.ChangelogEntry;
import com.codingcat.changelogs.base.data.ChangelogStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class EditorSession {
    private final @NotNull List<String> rawLines = new ArrayList<>();
    private final int entryUID;
    private int editingLineIndex = -1;
    private @NotNull String currentLine = "";
    private @NotNull String author = "";

    abstract void commit(@NotNull ChangelogStorage storage) throws CommitException;

    public abstract @NotNull String getId();

    public @NotNull @Unmodifiable List<Component> deserializeLines() {
        return getRawLines().stream()
                .map(MiniMessage.miniMessage()::deserialize)
                .toList();
    }

    public @Nullable Component deserializeAuthor() {
        return !author.isBlank() ? MiniMessage.miniMessage().deserialize(getAuthor()) : null;
    }

    public static class Create extends EditorSession {
        public Create(int entryUID) {
            super(entryUID);
        }

        @Override
        public void commit(@NotNull ChangelogStorage storage) throws CommitException {
            ChangelogEntry newEntry = new ChangelogEntry(
                    getEntryUID(),
                    this.deserializeLines(),
                    Instant.now(),
                    this.deserializeAuthor(),
                    new HashSet<>()
            );
            try {
                storage.storeEntry(newEntry);
            } catch (Throwable e) {
                throw new CommitException("internal_error");
            }
        }

        @Override
        public @NotNull String getId() {
            return "create";
        }
    }

    @Getter
    public static class Edit extends EditorSession {
        public Edit(@NotNull ChangelogEntry entry) {
            super(entry.uid());
            entry.lines().stream()
                    .map(MiniMessage.miniMessage()::serialize)
                    .forEach(getRawLines()::add);
            if (entry.author() != null) this.setAuthor(MiniMessage.miniMessage().serialize(entry.author()));
        }

        @Override
        public void commit(@NotNull ChangelogStorage storage) throws CommitException {
            ChangelogEntry currentEntry = storage.getByUID(getEntryUID());
            if (currentEntry == null) throw new CommitException("entry_deleted");
            ChangelogEntry newEntry = new ChangelogEntry(
                    currentEntry.uid(),
                    this.deserializeLines(),
                    currentEntry.recordedAt(),
                    this.deserializeAuthor(),
                    currentEntry.playersRead()
            );
            try {
                storage.updateEntry(newEntry);
            } catch (Throwable e) {
                throw new CommitException("internal_error");
            }
        }

        @Override
        public @NotNull String getId() {
            return "edit";
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CommitException extends Exception {
        private final @NotNull String translationKeyPart;
    }
}
