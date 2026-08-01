package com.codingcat.changelogs.platformapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public abstract class Entrypoint {
    private final @NotNull ChangelogsPlatform platform;

    public abstract void onStart();

    public abstract void onShutdown();
}
