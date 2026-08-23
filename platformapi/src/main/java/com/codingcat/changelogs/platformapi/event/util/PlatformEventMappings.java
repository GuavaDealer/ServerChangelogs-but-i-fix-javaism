package com.codingcat.changelogs.platformapi.event.util;

import com.codingcat.changelogs.platformapi.event.impl.IEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public abstract class PlatformEventMappings<E> {
    private final @NotNull Set<Mapping<E, ?, ?>> eventMappings = new HashSet<>();

    public record Mapping<E, PL extends IEvent, PA extends E>(
            @NotNull Class<PL> platformEventCls,
            @NotNull Class<PA> nativeEventCls,
            @NotNull Function<PA, PL> mapper
    ) {
        public @NotNull PL convertToPlatform(@NotNull PA nativeEvent) {
            return this.mapper.apply(nativeEvent);
        }
    }

    protected <T extends IEvent, N extends E> void register(@NotNull Class<T> platformEventCls, @NotNull Class<N> nativeEventCls, @NotNull Function<N, T> mapper) {
        this.eventMappings.add(new Mapping<>(platformEventCls, nativeEventCls, mapper));
    }

    public <T extends IEvent> @NotNull Mapping<E, T, ?> findForPlatformEvent(@NotNull Class<T> eventCls) {
        //noinspection unchecked
        return this.eventMappings.stream()
                .filter(m -> m.platformEventCls.equals(eventCls))
                .findAny().map(m -> (Mapping<E, T, ?>) m)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find matching native event for " + eventCls));
    }
}
