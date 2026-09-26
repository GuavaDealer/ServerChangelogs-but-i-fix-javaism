package com.codingcat.changelogs.base.data.storage;

import com.codingcat.changelogs.base.data.ChangelogEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class YamlChangelogStorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void storesFirstSeenTimestampOnlyOnce() {
        Path dataFile = this.temporaryDirectory.resolve("_data.yml");
        UUID player = UUID.randomUUID();
        Instant firstSeenAt = Instant.parse("2026-01-02T03:04:05Z");

        YamlChangelogStorage storage = new YamlChangelogStorage(dataFile);
        storage.init();

        assertTrue(storage.recordFirstSeen(player, firstSeenAt));
        assertFalse(storage.recordFirstSeen(player, firstSeenAt.plusSeconds(30)));
        assertEquals(firstSeenAt, storage.getFirstSeenAt(player));

        YamlChangelogStorage reloadedStorage = new YamlChangelogStorage(dataFile);
        reloadedStorage.init();
        assertEquals(firstSeenAt, reloadedStorage.getFirstSeenAt(player));
    }

    @Test
    void addsFirstSeenDataToLegacyStorageFile() throws IOException {
        Path dataFile = this.temporaryDirectory.resolve("_data.yml");
        Files.writeString(dataFile, "entries: []\n");
        UUID player = UUID.randomUUID();
        Instant firstSeenAt = Instant.parse("2026-02-03T04:05:06Z");

        YamlChangelogStorage storage = new YamlChangelogStorage(dataFile);
        storage.init();

        assertNull(storage.getFirstSeenAt(player));
        assertTrue(storage.recordFirstSeen(player, firstSeenAt));

        YamlChangelogStorage reloadedStorage = new YamlChangelogStorage(dataFile);
        reloadedStorage.init();
        assertEquals(firstSeenAt, reloadedStorage.getFirstSeenAt(player));
        assertTrue(reloadedStorage.listEntries().isEmpty());
    }

    @Test
    void onlyTreatsUnreadEntriesAfterFirstSeenAsUnread() {
        Path dataFile = this.temporaryDirectory.resolve("_data.yml");
        UUID player = UUID.randomUUID();
        Instant firstSeenAt = Instant.parse("2026-03-04T05:06:07Z");
        YamlChangelogStorage storage = new YamlChangelogStorage(dataFile);
        storage.init();
        storage.recordFirstSeen(player, firstSeenAt);

        ChangelogEntry older = entryAt(firstSeenAt.minusSeconds(1));
        ChangelogEntry simultaneous = entryAt(firstSeenAt);
        ChangelogEntry newer = entryAt(firstSeenAt.plusSeconds(1));

        assertFalse(storage.isUnreadFor(older, player));
        assertFalse(storage.isUnreadFor(simultaneous, player));
        assertTrue(storage.isUnreadFor(newer, player));

        newer.playersRead().add(player);
        assertFalse(storage.isUnreadFor(newer, player));
    }

    private static ChangelogEntry entryAt(Instant recordedAt) {
        return new ChangelogEntry(0, List.of(), recordedAt, null, new HashSet<>());
    }
}
