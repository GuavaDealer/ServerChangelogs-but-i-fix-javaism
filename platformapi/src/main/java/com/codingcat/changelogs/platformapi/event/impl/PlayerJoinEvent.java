package com.codingcat.changelogs.platformapi.event.impl;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public final class PlayerJoinEvent implements IEvent {
    private final @NotNull IPlayer player;
    private final boolean justConnected;
}
