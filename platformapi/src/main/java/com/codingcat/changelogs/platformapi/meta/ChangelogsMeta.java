package com.codingcat.changelogs.platformapi.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ChangelogsMeta {
    @NotNull String getName();

    @NotNull String getVersion();

    @Nullable String getDescription();

    @Nullable Set<String> getAuthors();
}
