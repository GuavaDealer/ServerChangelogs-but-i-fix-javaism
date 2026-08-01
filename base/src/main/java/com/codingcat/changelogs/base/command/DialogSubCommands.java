package com.codingcat.changelogs.base.command;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.dialog.ChangelogDialog;
import com.codingcat.changelogs.base.dialog.CreateChangelogDialog;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.codingcat.changelogs.platformapi.command.ICommandSource;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.codingcat.changelogs.base.lang.TranslationSource.translatable;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class DialogSubCommands {
    public static @NotNull Set<BrigadierCommandNode> withDialogs(@NotNull BrigadierCommandNode... commands) {
        Set<BrigadierCommandNode> commandSet = new HashSet<>();
        commandSet.add(new Command("create", "command.create", CreateChangelogDialog.class));
        commandSet.add(new Command("view", "command.view", ChangelogDialog.class));
        commandSet.addAll(Arrays.stream(commands).toList());
        return commandSet;
    }

    public static @NotNull LiteralCommandNode<?> buildDedicatedChangelogCommand(@NotNull ServerChangelogs plugin) {
        return new Command("changelog", "dedicated_command", ChangelogDialog.class).build(plugin);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Command implements BrigadierCommandNode {
        private final @NotNull String name;
        private final @NotNull String permission;
        private final @NotNull Class<? extends IDialog> dialogCls;

        @Override
        public @NotNull LiteralCommandNode<Object> build(@NotNull ServerChangelogs plugin) {
            ICommandManager commandManager = plugin.getPlatform().getCommandManager();
            return literal(this.name)
                    .requires(BrigadierCommandNode.requirePermission(this.permission, commandManager, false))
                    .executes(ctx -> {
                        ICommandSource source = commandManager.adaptPlatformSource(ctx.getSource());
                        if (source.getExecutingPlayer() == null) {
                            source.asAudience().sendMessage(translatable("command.onlyplayer"));
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        }
                        plugin.getDialogHolder().getFromType(this.dialogCls).showTo(source.getExecutingPlayer());
                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                    }).build();
        }
    }
}
