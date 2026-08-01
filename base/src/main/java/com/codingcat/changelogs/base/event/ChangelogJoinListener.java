package com.codingcat.changelogs.base.event;

import com.codingcat.changelogs.base.data.ChangelogStorage;
import com.codingcat.changelogs.base.dialog.ChangelogDialog;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.platformapi.event.MethodDispatchingListener;
import com.codingcat.changelogs.platformapi.event.impl.PlayerJoinEvent;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ChangelogJoinListener implements MethodDispatchingListener {
    private final @NotNull Supplier<ChangelogStorage> storageSupplier;
    private final @NotNull IDialog.Holder dialogHolder;

    @ListenerMethod
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (event.getPlayer().isFirstJoin()) return;
        boolean unreadChangelogs = this.storageSupplier.get().listEntries().stream()
                .anyMatch(e -> !e.hasRead(event.getPlayer()));
        if (!unreadChangelogs) return;
        ChangelogDialog dialog = this.dialogHolder.getFromType(ChangelogDialog.class);
        dialog.showTo(event.getPlayer());
    }
}
