package com.codingcat.changelogs.base.data;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.data.storage.YamlChangelogStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChangelogStorage {
    void init();

    void shutdown();

    void storeEntry(@NotNull ChangelogEntry entry);

    void updateEntry(@NotNull ChangelogEntry entry);

    boolean removeEntry(int uid);

    @NotNull List<ChangelogEntry> listEntries();

    @Nullable ChangelogEntry getByUID(int uid);

    void markAsRead(int uid, @NotNull UUID player);

    @Nullable Instant getFirstSeenAt(@NotNull UUID player);

    @NotNull Instant recordFirstSeen(@NotNull UUID player, @NotNull Instant seenAt);

    default boolean isUnreadFor(@NotNull ChangelogEntry entry, @NotNull UUID player) {
        Instant firstSeenAt = this.getFirstSeenAt(player);
        return firstSeenAt != null
                && entry.recordedAt().isAfter(firstSeenAt)
                && !entry.playersRead().contains(player);
    }

    int nextUID();

    @NotNull String getDisplayName();

    static @NotNull ChangelogStorage create(@NotNull String identifier, @NotNull ServerChangelogs plugin) throws IllegalArgumentException {
        if (!identifier.equals("yaml"))
            throw new IllegalArgumentException("Unknown changelog storage type \"" + identifier + "\"");
        return new YamlChangelogStorage(plugin.getPlatform().getDataPath().resolve("_data.yml"));
    }
}
