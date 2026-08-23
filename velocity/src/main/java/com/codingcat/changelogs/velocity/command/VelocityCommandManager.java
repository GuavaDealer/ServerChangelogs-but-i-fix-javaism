package com.codingcat.changelogs.velocity.command;

import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.codingcat.changelogs.platformapi.command.ICommandSource;
import com.codingcat.changelogs.velocity.VelocityChangelogsPlatform;
import com.codingcat.changelogs.velocity.player.VelocityPlayerManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@RequiredArgsConstructor
public class VelocityCommandManager implements ICommandManager {
    private final @NotNull CommandManager commandManager;
    private final @NotNull VelocityChangelogsPlatform platform;
    private final @NotNull VelocityPlayerManager playerManager;

    @Override
    public void register(@NotNull LiteralCommandNode<?> node, @NotNull Set<String> aliases) {
        //noinspection unchecked
        BrigadierCommand command = new BrigadierCommand((LiteralCommandNode<CommandSource>) node);
        CommandMeta commandMeta = this.commandManager.metaBuilder(command)
                .aliases(aliases.toArray(String[]::new))
                .plugin(platform)
                .build();
        this.commandManager.register(commandMeta, command);
    }

    @Override
    public @NotNull ICommandSource adaptPlatformSource(@NotNull Object platformCommandSource) throws IllegalArgumentException {
        if (!(platformCommandSource instanceof CommandSource source))
            throw new IllegalArgumentException("Expected native velocity CommandSource but got" + platformCommandSource);
        return new WrappedCommandSource(source, playerManager);
    }
}
