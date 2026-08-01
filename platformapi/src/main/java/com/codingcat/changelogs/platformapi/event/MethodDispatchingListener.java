package com.codingcat.changelogs.platformapi.event;

import com.codingcat.changelogs.platformapi.event.impl.IEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public interface MethodDispatchingListener {
    default void setupAll(@NotNull IEventManager eventManager) throws IllegalArgumentException {
        Class<? extends MethodDispatchingListener> cls = this.getClass();
        for (Method method : cls.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(ListenerMethod.class)) continue;
            if (method.getParameterCount() != 1)
                throw new IllegalArgumentException("Listener method " + method + " has an invalid amount of parameters");
            Class<?> paramType = method.getParameterTypes()[0];
            if (!IEvent.class.isAssignableFrom(paramType) || IEvent.class.equals(paramType))
                throw new IllegalArgumentException("Listener method " + method + " parameter has to be a valid implementation of IEvent, got " + paramType + " instead");
            IdentifiableEventConsumer<IEvent> handler = new IdentifiableEventConsumer<>() {
                @Override
                public @NotNull MethodDispatchingListener getOrigin() {
                    return MethodDispatchingListener.this;
                }

                @Override
                public void accept(IEvent iEvent) {
                    try {
                        method.invoke(this, iEvent);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("Failed to invoke method dispatching listener method " + method + " for event " + iEvent, e);
                    }
                }
            };
            //noinspection unchecked
            eventManager.registerListener((Class<IEvent>) paramType, handler);
        }
    }

    default void unregisterAll(@NotNull IEventManager eventManager) {
        eventManager.unregisterIf(l -> l instanceof IdentifiableEventConsumer<?> iec && iec.getOrigin() == this);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ListenerMethod {
    }

    interface IdentifiableEventConsumer<T extends IEvent> extends Consumer<T> {
        @NotNull MethodDispatchingListener getOrigin();
    }
}
