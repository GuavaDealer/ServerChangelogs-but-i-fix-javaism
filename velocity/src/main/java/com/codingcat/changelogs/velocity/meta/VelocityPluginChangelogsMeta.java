package com.codingcat.changelogs.velocity.meta;

import com.codingcat.changelogs.platformapi.meta.ChangelogsMeta;
import com.velocitypowered.api.plugin.PluginDescription;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class VelocityPluginChangelogsMeta implements ChangelogsMeta {
    private final @NotNull PluginDescription pluginDescription;

    @Override
    public @NotNull String getName() {
        return pluginDescription.getName().orElseGet(pluginDescription::getId);
    }

    @Override
    public @NotNull String getVersion() {
        return pluginDescription.getVersion().orElse("Unknown");
    }

    @Override
    public @Nullable String getDescription() {
        return pluginDescription.getDescription().orElse(null);
    }

    @Override
    public @Nullable Set<String> getAuthors() {
        return new HashSet<>(pluginDescription.getAuthors());
    }
}
