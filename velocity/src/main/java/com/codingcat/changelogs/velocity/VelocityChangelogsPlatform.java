package com.codingcat.changelogs.velocity;

import com.codingcat.changelogs.base.Entrypoints;
import com.codingcat.changelogs.base.compat.PacketEventsNativeItemManager;
import com.codingcat.changelogs.platformapi.ChangelogsPlatform;
import com.codingcat.changelogs.platformapi.Entrypoint;
import com.codingcat.changelogs.velocity.command.VelocityCommandManager;
import com.codingcat.changelogs.velocity.event.VelocityEventManager;
import com.codingcat.changelogs.velocity.meta.VelocityPlatformMeta;
import com.codingcat.changelogs.velocity.meta.VelocityPluginChangelogsMeta;
import com.codingcat.changelogs.velocity.player.VelocityPlayerManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPreShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class VelocityChangelogsPlatform implements ChangelogsPlatform {
    private final @NotNull Entrypoint entrypoint = Entrypoints.create(this);

    private final @Getter ComponentLogger componentLogger;
    private final @Getter Path dataPath;

    private final @Getter VelocityPlatformMeta platformMeta;
    private final @Getter VelocityPluginChangelogsMeta changelogsMeta;
    private final @Getter VelocityPlayerManager playerManager;
    private final @Getter VelocityEventManager eventManager;
    private final @Getter PacketEventsNativeItemManager nativeItemManager;
    private final @Getter VelocityCommandManager commandManager;

    private boolean earlyInitSucceeded = false;

    @Inject
    public VelocityChangelogsPlatform(@NotNull ProxyServer server, @NotNull ComponentLogger componentLogger, @NotNull @DataDirectory Path dataDirectory, @NotNull PluginContainer pluginContainer) {
        this.componentLogger = componentLogger;
        this.dataPath = dataDirectory;
        this.platformMeta = new VelocityPlatformMeta(server.getVersion());
        this.changelogsMeta = new VelocityPluginChangelogsMeta(pluginContainer.getDescription());
        this.playerManager = new VelocityPlayerManager(server);
        this.eventManager = new VelocityEventManager(server.getEventManager(), this);
        this.nativeItemManager = new PacketEventsNativeItemManager();
        this.commandManager = new VelocityCommandManager(server.getCommandManager(), this, this.playerManager);
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        try {
            Class.forName("com.github.retrooper.packetevents.PacketEvents");
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new RuntimeException("Unable to find PacketEvents! Please ensure you have the latest version of that plugin installed!", e);
        }
        this.eventManager.initMappings(getPlayerManager());
        this.earlyInitSucceeded = true;
        try {
            this.entrypoint.onStart();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to start platform entrypoint", e);
        }
    }

    @Subscribe
    @SuppressWarnings("UnstableApiUsage")
    public void onProxyPreShutdown(@NotNull ProxyPreShutdownEvent event) {
        if (!this.earlyInitSucceeded) return;
        try {
            this.entrypoint.onShutdown();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to shut down platform entrypoint", e);
        } finally {
            this.eventManager.unregisterAll();
        }
    }
}

