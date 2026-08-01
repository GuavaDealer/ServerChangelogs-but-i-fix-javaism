package com.codingcat.changelogs.platformapi.command;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICommandSource {
    @Nullable IPlayer getExecutingPlayer();

    @NotNull Audience asAudience();

    @NotNull PermissionChecker asPermissionChecker();

    default boolean hasPermission(@NotNull String permission) {
        return asPermissionChecker().test(permission);
    }

    default boolean hasPermission(@NotNull String permission, boolean trueIfUnset) {
        TriState value = asPermissionChecker().value(permission);
        return value == TriState.TRUE || (trueIfUnset && value == TriState.NOT_SET);
    }
}
