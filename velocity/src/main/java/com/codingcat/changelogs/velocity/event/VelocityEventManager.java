package com.codingcat.changelogs.velocity.event;

import com.codingcat.changelogs.platformapi.event.IEventManager;
import com.codingcat.changelogs.platformapi.event.impl.IEvent;
import com.codingcat.changelogs.platformapi.event.util.PlatformEventMappings;
import com.codingcat.changelogs.velocity.VelocityChangelogsPlatform;
import com.codingcat.changelogs.velocity.player.VelocityPlayerManager;
import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.EventManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class VelocityEventManager implements IEventManager {
    private final @NotNull EventManager eventManager;
    private final @NotNull VelocityChangelogsPlatform platform;
    private final @NotNull Set<PlatformMappingHandler<?>> handlerSet = new HashSet<>();
    private @Nullable VelocityEventMappings eventMappings;

    public void initMappings(@NotNull VelocityPlayerManager playerManager) {
        this.eventMappings = new VelocityEventMappings(playerManager);
    }

    @Override
    public <T extends IEvent> void registerListener(@NotNull Class<T> eventCls, @NotNull Consumer<T> listener) {
        VelocityEventMappings mappings = Objects.requireNonNull(eventMappings, "Event mappings not initialized");
        //noinspection unchecked
        PlatformEventMappings.Mapping<Object, T, Object> mapping = (PlatformEventMappings.Mapping<Object, T, Object>) mappings.findForPlatformEvent(eventCls);
        PlatformMappingHandler<T> mappingHandler = new PlatformMappingHandler<>(listener, mapping);
        this.eventManager.register(platform, mapping.nativeEventCls(), mappingHandler);
        this.handlerSet.add(mappingHandler);
    }

    @Override
    public void unregisterListener(@NotNull Consumer<?> listener) {
        this.unregisterIf(l -> l.equals(listener));
    }

    @Override
    public void unregisterIf(@NotNull Predicate<Consumer<?>> listenerPredicate) {
        Predicate<PlatformMappingHandler<?>> predicate = l -> listenerPredicate.test(l.listener);
        this.handlerSet.stream()
                .filter(predicate)
                .forEach(h -> this.eventManager.unregister(platform, h));
        this.handlerSet.removeIf(predicate);
    }

    @Override
    public void unregisterAll() {
        this.handlerSet.forEach(h -> eventManager.unregister(platform,h));
        this.handlerSet.clear();
    }

    @RequiredArgsConstructor
    private static final class PlatformMappingHandler<T extends IEvent> implements EventHandler<Object> {
        private final @Getter Consumer<T> listener;
        private final @NotNull PlatformEventMappings.Mapping<Object, T, ?> mapping;

        @Override
        public void execute(@NotNull Object event) {
            try {
                //noinspection unchecked
                T platformEvent = ((PlatformEventMappings.Mapping<Object, T, Object>) mapping).convertToPlatform(event);
                this.listener.accept(platformEvent);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to handle event " + event + " (platform " + mapping.platformEventCls() + ")", e);
            }
        }
    }
}
