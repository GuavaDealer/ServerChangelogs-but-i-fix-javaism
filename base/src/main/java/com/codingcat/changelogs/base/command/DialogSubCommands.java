package com.codingcat.changelogs.base.command;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.dialog.DialogPackets;
import com.codingcat.changelogs.base.dialog.ui.ChangelogDialog;
import com.codingcat.changelogs.base.dialog.ui.editor.ChangelogEditorDialog;
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
        commandSet.add(new Command("create", "command.create", false, ChangelogEditorDialog.class));
        commandSet.add(new Command("view", "command.view", true, ChangelogDialog.class));
        commandSet.addAll(Arrays.stream(commands).toList());
        return commandSet;
    }

    public static @NotNull LiteralCommandNode<?> buildDedicatedChangelogCommand(@NotNull ServerChangelogs plugin) {
        return new Command("changelog", "dedicated_command", true, ChangelogDialog.class).build(plugin);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Command implements BrigadierCommandNode {
        private final @NotNull String name;
        private final @NotNull String permission;
        private final boolean permissionEnabledByDefault;
        private final @NotNull Class<? extends IDialog> dialogCls;

        @Override
        public @NotNull LiteralCommandNode<Object> build(@NotNull ServerChangelogs plugin) {
            ICommandManager commandManager = plugin.getPlatform().getCommandManager();
            return literal(this.name)
                    .requires(BrigadierCommandNode.requirePermission(this.permission, plugin, permissionEnabledByDefault))
                    .executes(ctx -> {
                        ICommandSource source = commandManager.adaptPlatformSource(ctx.getSource());
                        if (source.getExecutingPlayer() == null) {
                            source.asAudience().sendMessage(translatable("command.onlyplayer"));
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        }
                        plugin.getDialogHolder().getFromType(this.dialogCls)
                                .showTo(source.getExecutingPlayer(), plugin.getDialogSessionManager(), DialogPackets.PacketPhase.PLAY);
                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                    }).build();
        }
    }
}
