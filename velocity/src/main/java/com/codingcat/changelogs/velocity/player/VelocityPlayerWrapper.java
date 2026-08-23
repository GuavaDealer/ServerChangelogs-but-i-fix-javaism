package com.codingcat.changelogs.velocity.player;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.PlayerSettings;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class VelocityPlayerWrapper implements IPlayer {
    private final @NotNull Player player;

    @Override
    public @NotNull UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    @Override
    public @NotNull Locale getClientLocale() {
        return Optional.ofNullable(this.player.getPlayerSettings())
                .map(PlayerSettings::getLocale)
                .orElseGet(Locale::getDefault);
    }

    @Override
    public boolean isFirstJoin() {
        return false; // TODO: Either implement a different system at the base level or make an implementation for velocity
    }

    @Override
    public @NotNull TriState isNativeAdmin() {
        return TriState.NOT_SET;
    }

    @Override
    public void kick(@Nullable Component reason) {
        this.player.disconnect(reason != null ? reason : Component.empty());
    }

    @Override
    public @NotNull Object asPacketEventsUser() {
        return Objects.requireNonNull(PacketEvents.getAPI().getPlayerManager().getUser(this.player), "PacketEvents player was null");
    }

    @Override
    public @NotNull Audience asAudience() {
        return this.player;
    }

    @Override
    public @NotNull PermissionChecker asPermissionChecker() {
        return this.player.getPermissionChecker();
    }

    @Override
    public @NotNull Object unwrapNative() {
        return this.player;
    }
}
