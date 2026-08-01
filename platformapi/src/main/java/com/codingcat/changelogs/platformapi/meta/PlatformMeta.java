package com.codingcat.changelogs.platformapi.meta;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface PlatformMeta {
    @NotNull Component getName();

    @NotNull String getVersion();
}
