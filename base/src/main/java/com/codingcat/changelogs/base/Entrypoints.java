package com.codingcat.changelogs.base;

import com.codingcat.changelogs.platformapi.ChangelogsPlatform;
import com.codingcat.changelogs.platformapi.Entrypoint;
import org.jetbrains.annotations.NotNull;

public final class Entrypoints {
    public static @NotNull Entrypoint create(@NotNull ChangelogsPlatform platform) {
        return new ServerChangelogs(platform);
    }
}
