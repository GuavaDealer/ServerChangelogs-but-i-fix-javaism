package com.codingcat.changelogs.platformapi.player;

import com.codingcat.changelogs.platformapi.audience.IBasePermissionHolderAudienceWrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public interface IPlayer extends IBasePermissionHolderAudienceWrapper {
    @NotNull UUID getUniqueId();

    @NotNull Locale getClientLocale();

    boolean isFirstJoin();

    void kick(@Nullable Component reason);
}
