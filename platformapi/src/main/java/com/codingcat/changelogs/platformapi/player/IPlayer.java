package com.codingcat.changelogs.platformapi.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public interface IPlayer extends Audience {
    @NotNull UUID getUniqueId();

    @NotNull Locale getClientLocale();

    boolean isFirstJoin();

    void kick(@Nullable Component reason);
}
