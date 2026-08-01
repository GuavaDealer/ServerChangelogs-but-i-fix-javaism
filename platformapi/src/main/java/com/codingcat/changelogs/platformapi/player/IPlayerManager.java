package com.codingcat.changelogs.platformapi.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface IPlayerManager {
    @NotNull Set<IPlayer> getOnlinePlayers();

    @Nullable IPlayer getFromUUID(@NotNull UUID uuid);

    @Nullable IPlayer getFromName(@NotNull String name);

    @NotNull IPlayer fromNative(@NotNull Object nativePlayer) throws IllegalArgumentException;
}
