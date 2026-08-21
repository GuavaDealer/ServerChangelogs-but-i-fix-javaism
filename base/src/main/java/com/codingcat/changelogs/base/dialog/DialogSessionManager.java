package com.codingcat.changelogs.base.dialog;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.codingcat.changelogs.platformapi.player.IPlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.chat.clickevent.CustomClickEvent;
import com.github.retrooper.packetevents.protocol.dialog.action.Action;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.action.StaticAction;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientCustomClickAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCustomClickAction;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DialogSessionManager implements PacketListener {
    private static final @NotNull Function<String, Key> KEY_GENERATOR = path -> ServerChangelogs.KEY_GENERATOR.apply("dialog/" + path);
    public static final @NotNull BinaryTagHolder NONE_TAG_HOLDER = BinaryTagHolder.binaryTagHolder("");
    private final Set<Key> staticActions = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Object> sessionData = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<?>> frozenViewers = new ConcurrentHashMap<>();
    private final @NotNull IDialog.Holder dialogHolder;
    private final @NotNull IPlayerManager playerManager;
    private @Nullable PacketListenerCommon selfListener;

    public void handleCustomClick(@NotNull WrapperCommonClientCustomClickAction<?> packetWrapper, @NotNull IPlayer player) {
        Key key = packetWrapper.getId().key();
        int fSIdx, lSIdx;
        if (!key.namespace().equals(ServerChangelogs.NAMESPACE) || (fSIdx = key.value().indexOf('/')) == -1
                || !key.value().startsWith("dialog/") || (fSIdx + 1) >= (lSIdx = key.value().lastIndexOf('/'))
                || lSIdx == (key.value().length() - 1)) return;
        String dialogId = key.value().substring(fSIdx + 1, lSIdx);
        String actionId = key.value().substring(lSIdx + 1);
        if (!dialogId.equals(activeSessions.get(player.getUniqueId())) && !staticActions.contains(key)) return;
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

    @Override
    public void onPacketReceive(@NotNull PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CUSTOM_CLICK_ACTION)
            handleCustomClick(new WrapperPlayClientCustomClickAction(event), playerFromPacket(event));
        if (event.getPacketType() == PacketType.Configuration.Client.CUSTOM_CLICK_ACTION)
            handleCustomClick(new WrapperConfigClientCustomClickAction(event), playerFromPacket(event));
    }

    private @NotNull IPlayer playerFromPacket(@NotNull PacketReceiveEvent event) {
        try {
            return this.playerManager.fromNative(event.getPlayer());
        } catch (IllegalArgumentException _) {
            // If obtaining the native IPlayer implementation fails (e.g. during the configuration phase,
            // where event.getPlayer() can return null), fall back to a wrapped PE User implementation.
            return DialogPackets.wrapPacketEventsUser(event.getUser());
        }
    }

    public void registerEvents() {
        this.selfListener = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }

    public void unregisterEvents() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this.selfListener);
        this.selfListener = null;
    }

    public @NotNull Action createStaticAction(@NotNull IDialog dialog, @NotNull String id) {
        Key key = this.createStaticKey(dialog, id);
        return new StaticAction(new CustomClickEvent(new ResourceLocation(key), null));
    }

    public @NotNull ClickEvent<?> createStaticClickEvent(@NotNull IDialog dialog, @NotNull String id) {
        return ClickEvent.custom(this.createStaticKey(dialog, id), NONE_TAG_HOLDER);
    }

    public @NotNull Action createSessionBasedAction(@NotNull IDialog dialog, @NotNull String id, boolean includeInputs) {
        Key key = this.createSessionBasedKey(dialog, id);
        CustomClickEvent clickEvent = new CustomClickEvent(new ResourceLocation(key), null);
        return includeInputs ? new DynamicCustomAction(clickEvent.getId(), null) : new StaticAction(clickEvent);
    }

    public @NotNull ClickEvent<?> createSessionBasedClickEvent(@NotNull IDialog dialog, @NotNull String id) {
        return ClickEvent.custom(this.createSessionBasedKey(dialog, id), NONE_TAG_HOLDER);
    }

    private @NotNull Key createSessionBasedKey(@NotNull IDialog dialog, @NotNull String id) {
        return KEY_GENERATOR.apply(dialog.getId() + "/" + id);
    }

    private @NotNull Key createStaticKey(@NotNull IDialog dialog, @NotNull String id) {
        Key key = this.createSessionBasedKey(dialog, id);
        this.staticActions.add(key);
        return key;
    }

    public void startSessionIfNoneActive(@NotNull IDialog dialog, @NotNull IPlayer player, @NotNull Supplier<Object> initialDataSupplier) {
        if (this.isSessionActive(player, dialog)) return;
        this.startSession(dialog, player, initialDataSupplier.get());
    }

    public void startSession(@NotNull IDialog dialog, @NotNull IPlayer player, @NotNull Object initialData) {
        this.setSessionData(player, initialData);
        this.activeSessions.put(player.getUniqueId(), dialog.getId());
    }

    public void endSession(@NotNull IPlayer player) {
        this.activeSessions.remove(player.getUniqueId());
        this.sessionData.remove(player.getUniqueId());
    }

    public boolean isSessionActive(@NotNull IPlayer player, @NotNull IDialog dialog) {
        return dialog.getId().equals(this.activeSessions.get(player.getUniqueId()));
    }

    public void setSessionData(@NotNull IPlayer player, @NotNull Object data) {
        this.sessionData.put(player.getUniqueId(), data);
    }

    public <T> @NotNull T getSessionData(@NotNull IPlayer player, @NotNull Class<T> dataType) throws ClassCastException {
        return dataType.cast(this.sessionData.get(player.getUniqueId()));
    }

    public void unfreeze(@NotNull IPlayer player) {
        CompletableFuture<?> future = this.frozenViewers.get(player.getUniqueId());
        if (future != null) future.complete(null);
    }

    public void freezeAndWaitFor(@NotNull IPlayer player) {
        CompletableFuture<?> future = new CompletableFuture<>();
        this.frozenViewers.put(player.getUniqueId(), future);
        try {
            future.join();
        } catch (CancellationException | CompletionException _) {
        }
        if (future.isDone()) this.frozenViewers.remove(player.getUniqueId());
    }
}
