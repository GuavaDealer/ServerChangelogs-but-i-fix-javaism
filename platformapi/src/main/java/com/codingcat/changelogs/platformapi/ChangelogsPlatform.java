package com.codingcat.changelogs.platformapi;

import com.codingcat.changelogs.platformapi.command.ICommandManager;
import com.codingcat.changelogs.platformapi.event.IEventManager;
import com.codingcat.changelogs.platformapi.meta.ChangelogsMeta;
import com.codingcat.changelogs.platformapi.meta.PlatformMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface ChangelogsPlatform {
    @NotNull ComponentLogger getLogger();

    @NotNull Path getDataPath();

    @NotNull IEventManager getEventManager();

    @NotNull ICommandManager getCommandManager();

    @NotNull PlatformMeta getPlatformMeta();

    @NotNull ChangelogsMeta getChangelogsMeta();
}
