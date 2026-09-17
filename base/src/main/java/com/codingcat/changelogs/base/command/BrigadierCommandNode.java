package com.codingcat.changelogs.base.command;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Predicate;

public interface BrigadierCommandNode {
    @NotNull Set<BrigadierCommandNode> SUB_COMMANDS = DialogSubCommands.withDialogs(new ReloadSubCommand(), new InfoSubCommand());

    @NotNull LiteralCommandNode<Object> build(@NotNull ServerChangelogs plugin);

    static @NotNull Predicate<Object> requirePermission(@NotNull String permission, @NotNull ServerChangelogs plugin, boolean trueIfUnset) {
        ICommandManager commandManager = plugin.getPlatform().getCommandManager();
        if (plugin.pluginConfig().useNativeFallbackPermissions()) return source -> {
            IPlayer player = commandManager.adaptPlatformSource(source).getExecutingPlayer();
            if (player == null) return true;
            return player.isNativeAdmin().toBooleanOrElse(false) || trueIfUnset;
        };
        String perm = ServerChangelogs.NAMESPACE + "." + permission;
        return source -> commandManager.adaptPlatformSource(source).hasPermission(perm, trueIfUnset);
    }
}
