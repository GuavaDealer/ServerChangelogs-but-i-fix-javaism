package com.codingcat.changelogs.base.command;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
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
        return literal("reload")
                .requires(BrigadierCommandNode.requirePermission("command.reload", plugin, false))
                .executes(ctx -> execute(ctx, plugin, false))
                .then(literal("--force").executes(ctx -> execute(ctx, plugin, true)))
                .build();
    }

    private int execute(@NotNull CommandContext<Object> ctx, @NotNull ServerChangelogs plugin, boolean force) {
        ICommandManager commandManager = plugin.getPlatform().getCommandManager();
        Audience audience = commandManager.adaptPlatformSource(ctx.getSource()).asAudience();
        long time = System.currentTimeMillis();
        Component errorMsg = null;
        try {
            plugin.reload(force);
            long took = System.currentTimeMillis() - time;
            audience.sendMessage(translatable("command.reload.success", text(took)));
        } catch (IOException e) {
            errorMsg = translatable("command.reload.error.io");
            error("command.reload.error.details", e);
        } catch (YAMLException e) {
            errorMsg = translatable("command.reload.error.invalid", text(e.getMessage()));
        } catch (IDialog.DestroyRejectedException e) {
            errorMsg = translatable("command.reload.error.dialog_reject." + e.getKey());
        }
        if (errorMsg != null) audience.sendMessage(errorMsg);
        return Command.SINGLE_SUCCESS;
    }
}
