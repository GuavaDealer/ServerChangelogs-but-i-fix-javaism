package com.codingcat.changelogs.platformapi.event;

import com.codingcat.changelogs.platformapi.event.impl.IEvent;
import com.codingcat.changelogs.platformapi.event.util.MethodDispatchingListener;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IEventManager {
    default void registerMethodDispatcher(@NotNull MethodDispatchingListener methodDispatcher) {
        methodDispatcher.setupAll(this);
    }

    default <T extends IEvent> void registerIdentified(@NotNull Class<T> eventCls, @NotNull Key key, @NotNull Consumer<T> listener) {
        this.registerListener(eventCls, IdentifiedListener.create(key, listener));
    }

    <T extends IEvent> void registerListener(@NotNull Class<T> eventCls, @NotNull Consumer<T> listener);

    void unregisterListener(@NotNull Consumer<?> listener);

    default void unregisterIdentified(@NotNull Key identifier) {
        this.unregisterIf(e -> e instanceof IdentifiedListener<?> ie && ie.key().equals(identifier));
    }

    void unregisterIf(@NotNull Predicate<Consumer<?>> listenerPredicate);

    void unregisterAll();
}
