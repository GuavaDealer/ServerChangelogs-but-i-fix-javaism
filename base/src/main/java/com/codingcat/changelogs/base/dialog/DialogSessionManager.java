package com.codingcat.changelogs.base.dialog;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.codingcat.changelogs.platformapi.player.IPlayerManager;
import com.github.retrooper.packetevents.protocol.chat.clickevent.CustomClickEvent;
import com.github.retrooper.packetevents.protocol.dialog.action.Action;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.action.StaticAction;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCustomClickAction;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class DialogSessionManager {
    private static final @NotNull Function<String, Key> KEY_GENERATOR = path -> ServerChangelogs.KEY_GENERATOR.apply("dialog/" + path);
    private final Set<Key> staticActions = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Object> sessionData = new ConcurrentHashMap<>();
    private final @NotNull IDialog.Holder dialogHolder;
    private final @NotNull IPlayerManager playerManager;

    public void handleCustomClick(@NotNull WrapperPlayClientCustomClickAction packetWrapper, @NotNull Object source) {
        Key key = packetWrapper.getId().key();
        int fSIdx, lSIdx;
        if (!key.namespace().equals(ServerChangelogs.NAMESPACE) || (fSIdx = key.value().indexOf('/')) == -1
                || !key.value().startsWith("dialog/") || (fSIdx + 1) < (lSIdx = key.value().lastIndexOf('/'))
                || lSIdx != (key.value().length() - 1)) return;
        IPlayer player = this.playerManager.fromNative(source);
        String dialogId = key.value().substring(fSIdx + 1, lSIdx);
        String actionId = key.value().substring(lSIdx + 1);
        if (!dialogId.equals(this.activeSessions.get(player.getUniqueId()))) return;
        IDialog dialog;
        try {
            dialog = this.dialogHolder.getFromId(dialogId);
        } catch (NullPointerException e) {
            return;
        }
        try {
            dialog.onActionTriggered(actionId, (NBTCompound) packetWrapper.getPayload(), player, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to run dialog action handler for \"" + dialogId + "\"", e);
        }
    }

    public @NotNull Action createStatic(@NotNull IDialog dialog, @NotNull String id) {
        Key key = KEY_GENERATOR.apply(dialog.getId() + "/" + id);
        this.staticActions.add(key);
        return new StaticAction(new CustomClickEvent(new ResourceLocation(key), null));
    }

    public @NotNull Action createSessionBased(@NotNull IDialog dialog, @NotNull String id, boolean includeInputs) {
        Key key = KEY_GENERATOR.apply(dialog.getId() + "/" + id);
        CustomClickEvent clickEvent = new CustomClickEvent(new ResourceLocation(key), null);
        return includeInputs ? new DynamicCustomAction(clickEvent.getId(), null) : new StaticAction(clickEvent);
    }

    public void startSession(@NotNull IDialog dialog, @NotNull IPlayer player, @Nullable Object initialData) {
        this.setSessionData(player, initialData);
        this.activeSessions.put(player.getUniqueId(), dialog.getId());
    }

    public void endSession(@NotNull IPlayer player) {
        this.activeSessions.remove(player.getUniqueId());
        this.sessionData.remove(player.getUniqueId());
    }

    public void setSessionData(@NotNull IPlayer player, @Nullable Object data) {
        this.sessionData.put(player.getUniqueId(), data);
    }

    public <T> @Nullable T getSessionData(@NotNull IPlayer player, @NotNull Class<T> dataType) throws ClassCastException {
        return dataType.cast(this.sessionData.get(player.getUniqueId()));
    }
}
