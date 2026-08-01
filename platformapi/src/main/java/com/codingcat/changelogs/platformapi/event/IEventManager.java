package com.codingcat.changelogs.platformapi.event;

import com.codingcat.changelogs.platformapi.event.impl.IEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IEventManager {
    default void registerMethodDispatcher(@NotNull MethodDispatchingListener methodDispatcher) {
        methodDispatcher.setupAll(this);
    }

    <T extends IEvent> void registerListener(@NotNull Class<T> eventCls, @NotNull Consumer<T> listener);

    void unregisterListener(@NotNull Consumer<?> listener);

    void unregisterIf(@NotNull Predicate<Consumer<?>> listenerPredicate);

    void unregisterAll();
}
