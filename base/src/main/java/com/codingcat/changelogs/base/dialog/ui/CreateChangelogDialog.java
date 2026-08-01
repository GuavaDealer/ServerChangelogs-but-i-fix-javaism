package com.codingcat.changelogs.base.dialog.ui;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.data.ChangelogEntry;
import com.codingcat.changelogs.base.data.ChangelogStorage;
import com.codingcat.changelogs.base.dialog.DialogPackets;
import com.codingcat.changelogs.base.dialog.DialogSessionManager;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.ItemDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.codingcat.changelogs.base.dialog.ui.ChangelogDialog.createLinesComponent;
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
        sessionManager.startSessionIfNoneActive(this, p, EditorState::new);
        EditorState state = sessionManager.getSessionData(p, EditorState.class);
        List<Component> previewLines = state.mmLineSources().stream()
                .map(MiniMessage.miniMessage()::deserialize)
                .toList();
        int newId = this.storage.nextUID() + 1;
        List<DialogBody> body = List.of(
                new ItemDialogBody(ItemStack.builder().type(ItemTypes.WRITABLE_BOOK).build(),
                        new PlainMessage(translatableManual(p, "dialog.create.subtitle", text(newId)), 160),
                        false, false, 15, 15),
                new PlainMessageDialogBody(new PlainMessage(translatableManual(p, "dialog.create.hint"), ChangelogDialog.LINE_WIDTH)),
                new PlainMessageDialogBody(new PlainMessage(!previewLines.isEmpty() ? createLinesComponent(p, previewLines) : translatableManual(p, "dialog.create.empty_preview"), ChangelogDialog.LINE_WIDTH))
        );
        List<Input> inputs = List.of(
                new Input("line", new TextInputControl(350, translatableManual(p, "dialog.create.input.contents"),
                        true, state.currentLineSource().get(), 5000, null)),
                new Input("author", new TextInputControl(200, translatableManual(p, "dialog.create.input.author"), true, state.authorSource().get(), 200, null))
        );
        CommonDialogData common = new CommonDialogData(
                translatableManual(p, TITLE_KEY),
                null, false, false,
                DialogAction.NONE, body, inputs
        );
        ActionButton yesBtn = new ActionButton(new CommonButtonData(translatableManual(p, "dialog.create.button.publish"), null, 160), sessionManager.createSessionBased(this, "publish", true));
        ActionButton addLineBtn = new ActionButton(new CommonButtonData(translatableManual(p, "dialog.create.button.add_line"), null, 100), sessionManager.createSessionBased(this, "add_line", true));
        ActionButton cancelBtn = new ActionButton(new CommonButtonData(translatableManual(p, "dialog.create.button.cancel"), null, 100), sessionManager.createSessionBased(this, "close", false));
        return new MultiActionDialog(common, List.of(yesBtn, addLineBtn), cancelBtn, 3);
    }

    @Override
    public void onActionTriggered(@NotNull String action, @Nullable NBTCompound data, @NotNull IPlayer source, @NotNull DialogSessionManager sessionManager) {
        if (action.equals("close")) {
            sessionManager.endSession(source);
            DialogPackets.clearDialog(source, DialogPackets.PacketPhase.PLAY);
            return;
        }
        if (data != null) {
            String rawLine = data.getStringTagValueOrThrow("line");
            String rawAuthor = data.getStringTagValueOrThrow("author");
            EditorState state = sessionManager.getSessionData(source, EditorState.class);
            state.currentLineSource().set(rawLine);
            state.authorSource().set(rawAuthor);
        }
        if (action.equals("retry")) {
            this.showTo(source, sessionManager, DialogPackets.PacketPhase.PLAY);
            return;
        }
        if (action.equals("add_line")) {
            if (data == null) return;
            EditorState state = sessionManager.getSessionData(source, EditorState.class);
            if (state.currentLineSource().get().isBlank()) {
                this.showRetry(source, "add_empty_line", sessionManager);
                return;
            }
            state.mmLineSources().add(state.currentLineSource().get());
            state.currentLineSource().set("");
            this.showTo(source, sessionManager, DialogPackets.PacketPhase.PLAY);
            return;
        }
        if (!action.equals("publish")) return;
        if ((useFallbackPermissions && !source.isNativeAdmin().toBooleanOrElse(false)) || (!useFallbackPermissions && !source.hasPermission(ServerChangelogs.NAMESPACE + ".command.create"))) {
            sessionManager.endSession(source);
            DialogPackets.showSimpleNotice(source, TITLE_KEY, "dialog.create.error.no_permission");
            return;
        }
        if (data == null) return;
        EditorState editorState = sessionManager.getSessionData(source, EditorState.class);
        List<Component> lines = editorState.mmLineSources().stream()
                .map(MiniMessage.miniMessage()::deserialize)
                .toList();
        if (lines.isEmpty()) {
            this.showRetry(source, "no_lines", sessionManager);
            return;
        }
        String author = editorState.authorSource().get();
        ChangelogEntry entry = new ChangelogEntry(
                this.storage.nextUID(),
                lines, Instant.now(),
                !author.isBlank() ? MiniMessage.miniMessage().deserialize(author) : null,
                new HashSet<>()
        );
        this.storage.storeEntry(entry);
        sessionManager.endSession(source);
        DialogPackets.showSimpleNotice(source, TITLE_KEY, "dialog.create.success");
    }

    private void showRetry(@NotNull IPlayer source, @NotNull String errorPart, @NotNull DialogSessionManager sessionManager) {
        DialogPackets.showSimpleNotice(source, TITLE_KEY, "dialog.create.error." + errorPart, sessionManager.createSessionBased(this, "retry", false), DialogPackets.PacketPhase.PLAY);
    }

    private record EditorState(
            @NotNull List<String> mmLineSources,
            @NotNull AtomicReference<String> currentLineSource,
            @NotNull AtomicReference<String> authorSource
    ) {
        public EditorState() {
            this(new ArrayList<>(), new AtomicReference<>(""), new AtomicReference<>(""));
        }
    }
}
