package com.codingcat.changelogs.platformapi.event.impl;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be handles asynchronously by the platform implementations,
 * so that blocking this event's thread will result in a player staying in the configuration phase.
 */
@RequiredArgsConstructor
public final class PlayerEnterConfigurationPhaseEvent implements IEvent {
    @Getter
    private final @NotNull IPlayer player;
}
