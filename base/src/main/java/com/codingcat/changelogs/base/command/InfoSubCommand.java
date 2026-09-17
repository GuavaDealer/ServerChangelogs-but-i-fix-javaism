package com.codingcat.changelogs.base.command;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

import static com.codingcat.changelogs.base.lang.TranslationSource.translatable;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class InfoSubCommand implements BrigadierCommandNode {
    @Override
    public @NotNull LiteralCommandNode<Object> build(@NotNull ServerChangelogs plugin) {
        ICommandManager commandManager = plugin.getPlatform().getCommandManager();
        return literal("info")
                .requires(BrigadierCommandNode.requirePermission("command.info", plugin, false))
                .executes(ctx -> {
                    commandManager.adaptPlatformSource(ctx.getSource()).asAudience().sendMessage(translatable("command.root"));
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
