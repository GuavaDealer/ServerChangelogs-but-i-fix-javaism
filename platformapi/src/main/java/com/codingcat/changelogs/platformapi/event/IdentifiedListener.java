package com.codingcat.changelogs.platformapi.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IdentifiedListener<T> extends Consumer<T>, Keyed {
    static <T> @NotNull IdentifiedListener<T> create(@NotNull Key key, @NotNull Consumer<T> consumer) {
        return new IdentifiedListener<>() {
            @Override
            public void accept(T t) {
                consumer.accept(t);
            }

            @Override
            public @NotNull Key key() {
                return key;
            }
        };
    }
}
