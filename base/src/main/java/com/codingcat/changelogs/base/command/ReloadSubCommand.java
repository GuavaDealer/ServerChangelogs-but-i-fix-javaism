package com.codingcat.changelogs.base.command;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;

import static com.codingcat.changelogs.base.ServerChangelogs.error;
import static com.codingcat.changelogs.base.lang.TranslationSource.translatable;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static net.kyori.adventure.text.Component.text;

public class ReloadSubCommand implements BrigadierCommandNode {
    @Override
    public @NotNull LiteralCommandNode<Object> build(@NotNull ServerChangelogs plugin) {
        ICommandManager commandManager = plugin.getPlatform().getCommandManager();
        return literal("reload")
                .requires(BrigadierCommandNode.requirePermission("command.reload", commandManager, false))
                .executes(ctx -> {
                    Audience audience = commandManager.adaptPlatformSource(ctx).asAudience();
                    long time = System.currentTimeMillis();
                    Component errorMsg = null;
                    try {
                        plugin.reload();
                        long took = System.currentTimeMillis() - time;
                        audience.sendMessage(translatable("command.reload.success", text(took)));
                    } catch (IOException e) {
                        errorMsg = translatable("command.reload.error.io");
                        error("command.reload.error.details", e);
                    } catch (YAMLException e) {
                        errorMsg = translatable("command.reload.error.invalid", text(e.getMessage()));
                    }
                    if (errorMsg != null) audience.sendMessage(errorMsg);
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
