package com.codingcat.changelogs.velocity.meta;

import com.codingcat.changelogs.platformapi.meta.PlatformMeta;
import com.velocitypowered.api.util.ProxyVersion;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public class VelocityPlatformMeta implements PlatformMeta {
    private final @NotNull ProxyVersion proxyVersion;

    @Override
    public @NotNull Component getName() {
        return text(proxyVersion.getName(), NamedTextColor.RED);
    }

    @Override
    public @NotNull String getVersion() {
        return proxyVersion.getVersion();
    }
}
