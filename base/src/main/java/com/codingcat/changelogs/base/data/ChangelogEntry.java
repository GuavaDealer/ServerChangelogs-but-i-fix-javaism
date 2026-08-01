package com.codingcat.changelogs.base.data;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ChangelogEntry(
        int uid,
        @NotNull List<Component> lines,
        @NotNull Instant recordedAt,
        @Nullable Component author,
        @NotNull Set<UUID> playersRead
) {
    public boolean hasRead(@NotNull IPlayer player) {
        return this.playersRead().contains(player.getUniqueId());
    }
}
