package com.codingcat.changelogs.base.event;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.config.PluginConfig;
import com.codingcat.changelogs.base.data.ChangelogStorage;
import com.codingcat.changelogs.base.dialog.DialogSessionManager;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.base.dialog.ui.ChangelogDialog;
import com.codingcat.changelogs.platformapi.event.IEventManager;
import com.codingcat.changelogs.platformapi.event.impl.PlayerEnterConfigurationPhaseEvent;
import com.codingcat.changelogs.platformapi.event.impl.PlayerJoinEvent;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.codingcat.changelogs.base.ServerChangelogs.info;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public class ChangelogJoinListener {
    private static final @NotNull Key KEY = ServerChangelogs.KEY_GENERATOR.apply("listener/changelog_display");
    private final @NotNull Supplier<ChangelogStorage> storageSupplier;
    private final @NotNull IDialog.Holder dialogHolder;
    private final @NotNull DialogSessionManager sessionManager;
    private final @NotNull PluginConfig config;

    public void registerEvents(@NotNull IEventManager manager) {
        switch (config.getDialogPacketPhase()) {
            case PLAY ->
                    manager.registerIdentified(PlayerJoinEvent.class, KEY, e -> checkShowDialog(e.getPlayer(), false));
            case CONFIGURATION -> manager.registerIdentified(PlayerEnterConfigurationPhaseEvent.class, KEY, e -> {
                if (checkShowDialog(e.getPlayer(), true)) {
                    info("console.join.frozen", text(e.getPlayer().getUniqueId().toString()));
                    sessionManager.waitForUnfreeze(e.getPlayer());
                }
            });
        }
    }

    public void unregisterEvents(@NotNull IEventManager manager) {
        manager.unregisterIdentified(KEY);
    }

    public boolean checkShowDialog(@NotNull IPlayer player, boolean freeze) {
        if (player.isFirstJoin()) return false;
        boolean unreadChangelogs = this.storageSupplier.get().listEntries().stream()
                .anyMatch(e -> !e.hasRead(player));
        if (!unreadChangelogs) return false;
        ChangelogDialog dialog = this.dialogHolder.getFromType(ChangelogDialog.class);
        if (freeze) this.sessionManager.freeze(player);
        dialog.showTo(player, this.sessionManager, config.getDialogPacketPhase());
        return true;
    }
}
