package com.codingcat.changelogs.velocity.command;

import com.codingcat.changelogs.platformapi.command.ICommandSource;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.codingcat.changelogs.velocity.player.VelocityPlayerManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.permission.PermissionChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class WrappedCommandSource implements ICommandSource {
    private final @NotNull CommandSource commandSource;
    private final @NotNull VelocityPlayerManager playerManager;

    @Override
    public @Nullable IPlayer getExecutingPlayer() {
        return this.commandSource instanceof Player p ? playerManager.fromNative(p) : null;
    }

    @Override
    public @NotNull Audience asAudience() {
        return this.commandSource;
    }

    @Override
    public @NotNull PermissionChecker asPermissionChecker() {
        return this.commandSource.getPermissionChecker();
    }

    @Override
    public @NotNull Object unwrapNative() {
        return this.commandSource;
    }
}
