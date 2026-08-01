package com.codingcat.changelogs.base.dialog.ui;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.data.ChangelogEntry;
import com.codingcat.changelogs.base.data.ChangelogStorage;
import com.codingcat.changelogs.base.dialog.DialogPackets;
import com.codingcat.changelogs.base.dialog.DialogSessionManager;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.ConfirmationDialog;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.ItemDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.dialog.input.Input;
import com.github.retrooper.packetevents.protocol.dialog.input.TextInputControl;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import static com.codingcat.changelogs.base.lang.TranslationSource.translatableManual;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public class CreateChangelogDialog implements IDialog {
    private static final @NotNull String TITLE_KEY = "dialog.create.title";
    private final @Getter String id = "changelog_editor";
    private final @NotNull ChangelogStorage storage;
    private final boolean useFallbackPermissions;

    @Override
    public @NotNull Dialog build(@NotNull IPlayer p, @NotNull DialogSessionManager sessionManager) {
        int newId = this.storage.nextUID() + 1;
        List<DialogBody> body = List.of(
                new ItemDialogBody(ItemStack.builder().type(ItemTypes.WRITABLE_BOOK).build(),
                        new PlainMessage(translatableManual(p, "dialog.create.subtitle", text(newId)), 160),
                        false, false, 15, 15)
        );
        List<Input> inputs = List.of(
                new Input("contents", new TextInputControl(250, translatableManual(p, "dialog.create.input.contents"),
                        true, "", 20000, new TextInputControl.MultilineOptions(100, 300))),
                new Input("author", new TextInputControl(200, translatableManual(p, "dialog.create.input.author"), true, "", 200, null))
        );
        CommonDialogData common = new CommonDialogData(
                translatableManual(p, TITLE_KEY),
                null, false, false,
                DialogAction.NONE, body, inputs
        );
        ActionButton yesBtn = new ActionButton(new CommonButtonData(translatableManual(p, "dialog.create.button.publish"), null, 160), sessionManager.createSessionBased(this, "publish", true));
        ActionButton noBtn = new ActionButton(new CommonButtonData(translatableManual(p, "dialog.create.button.cancel"), null, 100), sessionManager.createSessionBased(this, "close", false));
        sessionManager.startSessionIfNoneActive(this, p, Object::new);
        return new ConfirmationDialog(common, yesBtn, noBtn);
    }

    @Override
    public void onActionTriggered(@NotNull String action, @Nullable NBTCompound data, @NotNull IPlayer source, @NotNull DialogSessionManager sessionManager) {
        if (action.equals("retry")) {
            this.showTo(source, sessionManager, DialogPackets.PacketPhase.PLAY);
            return;
        }
        if (action.equals("close")) {
            sessionManager.endSession(source);
            DialogPackets.clearDialog(source, DialogPackets.PacketPhase.PLAY);
            return;
        }
        if (!action.equals("publish")) return;
        if ((useFallbackPermissions && !source.isNativeAdmin().toBooleanOrElse(false)) || (!useFallbackPermissions && !source.hasPermission(ServerChangelogs.NAMESPACE + ".command.create"))) {
            sessionManager.endSession(source);
            DialogPackets.showSimpleNotice(source, TITLE_KEY, "dialog.create.error.no_permission");
            return;
        }
        if (data == null) return;
        List<Component> lines = data.getStringTagValueOrThrow("contents")
                .lines()
                .map(MiniMessage.miniMessage()::deserialize)
                .toList();
        if (lines.isEmpty()) {
            DialogPackets.showSimpleNotice(source, TITLE_KEY, "dialog.create.error.content_empty", sessionManager.createSessionBased(this, "retry", false), DialogPackets.PacketPhase.PLAY);
            return;
        }
        String author = data.getStringTagValueOrThrow("author");
        ChangelogEntry entry = new ChangelogEntry(
                this.storage.nextUID(),
                lines, Instant.now(),
                !author.isBlank() ? MiniMessage.miniMessage().deserialize(author) : null,
                new HashSet<>()
        );
        this.storage.storeEntry(entry);
        DialogPackets.showSimpleNotice(source, TITLE_KEY, "dialog.create.success");
    }
}
