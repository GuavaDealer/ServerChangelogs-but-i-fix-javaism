package com.codingcat.changelogs.base.data.storage;

import com.codingcat.changelogs.base.data.ChangelogEntry;
import com.codingcat.changelogs.base.data.ChangelogStorage;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class YamlChangelogStorage implements ChangelogStorage {
    private final @NotNull Path filePath;
    private List<ChangelogEntry> cache;

    @Override
    public void init() {
        if (!filePath.toFile().exists()) {
            this.cache = new ArrayList<>();
            this.save();
            return;
        }
        try {
            Yaml config = new Yaml();
            Map<String, Object> data = config.load(new FileInputStream(filePath.toFile()));
            this.cache = this.deserializeEntries(data);
        } catch (IOException | YAMLException e) {
            throw new RuntimeException("Failed to load changelog data from " + filePath, e);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void storeEntry(@NotNull ChangelogEntry entry) {
        this.cache.add(entry.uid(), entry);
        this.save();
    }

    @Override
    public void updateEntry(@NotNull ChangelogEntry entry) {
        this.cache.set(entry.uid(), entry);
        this.save();
    }

    @Override
    public boolean removeEntry(int uid) {
        boolean success = this.cache.removeIf(e -> e.uid() == uid);
        if (success) {
            this.fixEntryUIDs();
            this.save();
        }
        return success;
    }

    private void save() {
        Yaml config = new Yaml();
        List<Map<String, Object>> data = this.cache.stream()
                .map(this::serializeEntry)
                .toList();
        try (FileOutputStream os = new FileOutputStream(filePath.toFile())) {
            config.dump(Map.of("entries", data), new BufferedWriter(new OutputStreamWriter(os)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save changelog data to " + filePath, e);
        }
    }

    private @NotNull List<ChangelogEntry> deserializeEntries(@NotNull Map<String, Object> config) {
        try {
            //noinspection unchecked
            List<Map<?, ?>> entries = (List<Map<?, ?>>) config.get("entries");
            Objects.requireNonNull(entries, "entries");
            List<ChangelogEntry> results = new ArrayList<>();
            for (int i = 0; i < entries.size(); i++) {
                //noinspection unchecked
                Map<String, ?> data = (Map<String, ?>) entries.get(i);
                results.add(this.deserializeEntry(i, data));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize changelog entries", e);
        }
    }

    private @NotNull ChangelogEntry deserializeEntry(int uid, @NotNull Map<String, ?> data) {
        //noinspection unchecked
        List<Component> lines = ((List<String>) data.get("serializedLines"))
                .stream().map(GsonComponentSerializer.gson()::deserialize)
                .toList();
        String recordedAtRaw = Objects.requireNonNull((String) data.get("recordedAt"), "recordedAt");
        Instant recordedAt = DateTimeFormatter.ISO_INSTANT.parse(recordedAtRaw, Instant::from);
        Component author = Optional.ofNullable((String) data.get("author"))
                .map(GsonComponentSerializer.gson()::deserialize)
                .orElse(null);
        //noinspection unchecked
        Set<UUID> playersRead = ((List<String>) data.get("playersRead"))
                .stream().map(UUID::fromString)
                .collect(Collectors.toSet());
        return new ChangelogEntry(uid, lines, recordedAt, author, playersRead);
    }

    private @NotNull Map<String, Object> serializeEntry(@NotNull ChangelogEntry entry) {
        Map<String, Object> data = new HashMap<>();
        List<String> serializedLines = entry.lines()
                .stream().map(GsonComponentSerializer.gson()::serialize)
                .toList();
        data.put("serializedLines", serializedLines);
        String recordedAtRaw = DateTimeFormatter.ISO_INSTANT.format(entry.recordedAt());
        data.put("recordedAt", recordedAtRaw);
        String serializedAuthor = Optional.ofNullable(entry.author())
                .map(GsonComponentSerializer.gson()::serialize)
                .orElse(null);
        data.put("author", serializedAuthor);
        List<String> rawPlayersRead = entry.playersRead()
                .stream().map(UUID::toString).toList();
        data.put("playersRead", rawPlayersRead);
        return data;
    }

    @Override
    public @NotNull List<ChangelogEntry> listEntries() {
        return this.cache;
    }

    @Override
    public @Nullable ChangelogEntry getByUID(int uid) {
        try {
            return this.cache.get(uid);
        } catch (IndexOutOfBoundsException _) {
            return null;
        }
    }

    @Override
    public void markAsRead(int uid, @NotNull UUID player) {
        this.cache.stream()
                .filter(e -> e.uid() == uid)
                .forEach(e -> e.playersRead().add(player));
        this.save();
    }

    @Override
    public int nextUID() {
        return this.cache.size();
    }

    /**
     * Fix the UIDs of cache entries desyncing from their actual
     * position in the list when {@link #removeEntry(int)} is called.
     * This re-creates the entire cache including all of its entries.
     */
    private void fixEntryUIDs() {
        List<ChangelogEntry> entries = new ArrayList<>(this.cache);
        this.cache.clear();
        for (int i = 0; i < entries.size(); i++) {
            ChangelogEntry original = entries.get(i);
            this.cache.add(new ChangelogEntry(
                    i, original.lines(),
                    original.recordedAt(),
                    original.author(),
                    original.playersRead()
            ));
        }
    }

    @Override
    public @NotNull String getDisplayName() {
        return "YAML File";
    }
}
