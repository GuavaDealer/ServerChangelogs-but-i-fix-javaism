package com.codingcat.changelogs.base.command;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Predicate;

public interface BrigadierCommandNode {
    @NotNull Set<BrigadierCommandNode> SUB_COMMANDS = DialogSubCommands.withDialogs(new ReloadSubCommand());

    @NotNull LiteralCommandNode<Object> build(@NotNull ServerChangelogs plugin);

    static @NotNull Predicate<Object> requirePermission(@NotNull String permission, @NotNull ICommandManager commandManager, boolean trueIfUnset) {
        String perm = ServerChangelogs.NAMESPACE + "." + permission;
        return ctx -> commandManager.adaptPlatformSource(ctx).hasPermission(perm, trueIfUnset);
    }
}
