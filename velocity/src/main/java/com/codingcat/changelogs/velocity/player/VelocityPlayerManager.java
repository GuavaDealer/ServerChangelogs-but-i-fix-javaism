package com.codingcat.changelogs.velocity.player;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.codingcat.changelogs.platformapi.player.IPlayerManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class VelocityPlayerManager implements IPlayerManager {
    private final @NotNull ProxyServer server;

    @Override
    public @NotNull Set<IPlayer> getOnlinePlayers() {
        return this.server.getAllPlayers().stream()
                .map(this::fromNative)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Nullable IPlayer getFromUUID(@NotNull UUID uuid) {
        return this.server.getPlayer(uuid).map(this::fromNative).orElse(null);
    }

    @Override
    public @Nullable IPlayer getFromName(@NotNull String name) {
        return this.server.getPlayer(name).map(this::fromNative).orElse(null);
    }

    @Override
    public @NotNull IPlayer fromNative(@NotNull Object nativePlayer) throws IllegalArgumentException {
        if (!(nativePlayer instanceof Player player))
            throw new IllegalArgumentException("Expected native velocity player but got " + nativePlayer);
        return new VelocityPlayerWrapper(player);
    }
}
