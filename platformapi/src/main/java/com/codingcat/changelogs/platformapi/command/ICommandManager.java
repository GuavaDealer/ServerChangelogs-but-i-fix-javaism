package com.codingcat.changelogs.platformapi.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ICommandManager {
    default void register(@NotNull LiteralCommandNode<?> node) {
        this.register(node, Set.of());
    }

    void register(@NotNull LiteralCommandNode<?> node, @NotNull Set<String> aliases);

    @NotNull ICommandSource adaptPlatformSource(@NotNull Object platformCommandSource) throws IllegalArgumentException;
}
