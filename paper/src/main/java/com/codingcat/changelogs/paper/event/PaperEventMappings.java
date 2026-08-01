package com.codingcat.changelogs.paper.event;

import com.codingcat.changelogs.paper.player.PaperPlayerManager;
import com.codingcat.changelogs.platformapi.event.impl.IEvent;
import com.codingcat.changelogs.platformapi.event.impl.PlayerJoinEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

public final class PaperEventMappings {
    private final @NotNull Set<EventMapping<?, ?>> eventMappings;

    PaperEventMappings(@NotNull PaperPlayerManager pm) {
        this.eventMappings = Set.of(
                new EventMapping<>(
                        PlayerJoinEvent.class,
                        org.bukkit.event.player.PlayerJoinEvent.class,
                        ev -> new PlayerJoinEvent(pm.fromNative(ev.getPlayer()))
                )
        );
    }

    <T extends IEvent> @NotNull EventMapping<T, ?> findForPlatformEvent(@NotNull Class<T> eventCls) {
        //noinspection unchecked
        return this.eventMappings.stream()
                .filter(m -> m.platformEventCls.equals(eventCls))
                .findAny().map(m -> (EventMapping<T, ?>) m)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find matching paper event for " + eventCls));
    }

    record EventMapping<PL extends IEvent, PA extends Event>(
            @NotNull Class<PL> platformEventCls,
            @NotNull Class<PA> paperEventCls,
            @NotNull Function<PA, PL> mapper
    ) {
        public @NotNull PL convertToPlatform(@NotNull PA paperEvent) {
            return this.mapper.apply(paperEvent);
        }
    }
}
