package com.codingcat.changelogs.base.dialog.ui.editor;

import com.codingcat.changelogs.base.ServerChangelogs;
import com.codingcat.changelogs.base.data.ChangelogStorage;
import com.codingcat.changelogs.base.dialog.DialogPackets;
import com.codingcat.changelogs.base.dialog.DialogSessionManager;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.base.dialog.ui.ChangelogDialog;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

import static com.codingcat.changelogs.base.dialog.ui.ChangelogDialog.createLinesComponent;
import static com.codingcat.changelogs.base.lang.TranslationSource.translatableManual;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public class ChangelogEditorDialog implements IDialog {
    private static final @NotNull Function<EditorSession, String> TITLE_KEY = s -> "dialog.editor." + s.getId() + ".title";
    private final @Getter String id = "changelog_editor";
    private final @NotNull ChangelogStorage storage;
    private final boolean useFallbackPermissions;

    @Override
    public @NotNull Dialog build(@NotNull IPlayer p, @NotNull DialogSessionManager sessionManager) {
        sessionManager.startSessionIfNoneActive(this, p, () -> new EditorSession.Create(storage.nextUID()));
        EditorSession session = sessionManager.getSessionData(p, EditorSession.class);
        String sessionTranslation = "dialog.editor." + session.getId();
        List<Component> previewLines = session.deserializeLines();
        List<DialogBody> body = List.of(
                new ItemDialogBody(ItemStack.builder().type(ItemTypes.WRITABLE_BOOK).build(),
                        new PlainMessage(translatableManual(p, sessionTranslation + ".subtitle", text(session.getEntryUID() + 1)), 160),
                        false, false, 15, 15),
                new PlainMessageDialogBody(new PlainMessage(translatableManual(p, "dialog.editor.hint"), ChangelogDialog.LINE_WIDTH)),
                new PlainMessageDialogBody(new PlainMessage(!previewLines.isEmpty() ? createLinesComponent(p, previewLines) : translatableManual(p, "dialog.editor.empty_preview"), ChangelogDialog.LINE_WIDTH))
        );
        List<Input> inputs = List.of(
                new Input("line", new TextInputControl(350, translatableManual(p, "dialog.editor.input.contents"),
                        true, session.getCurrentLine(), 5000, null)),
                new Input("author", new TextInputControl(200, translatableManual(p, "dialog.editor.input.author"), true, session.getAuthor(), 200, null))
        );
        CommonDialogData common = new CommonDialogData(
                translatableManual(p, TITLE_KEY.apply(session)),
                null, false, false,
                DialogAction.NONE, body, inputs
        );
        ActionButton yesBtn = new ActionButton(new CommonButtonData(translatableManual(p, sessionTranslation + ".commit_button"), null, 160), sessionManager.createSessionBasedAction(this, "commit", true));
        ActionButton addLineBtn = new ActionButton(new CommonButtonData(translatableManual(p, "dialog.editor.button.add_line"), null, 100), sessionManager.createSessionBasedAction(this, "add_line", true));
        ActionButton cancelBtn = new ActionButton(new CommonButtonData(translatableManual(p, "dialog.editor.button.cancel"), null, 100), sessionManager.createSessionBasedAction(this, "close", false));
        return new MultiActionDialog(common, List.of(yesBtn, addLineBtn), cancelBtn, 3);
    }

    @Override
    public void onActionTriggered(@NotNull String action, @Nullable NBTCompound data, @NotNull IPlayer source, @NotNull DialogSessionManager sessionManager) {
        EditorSession session = sessionManager.getSessionData(source, EditorSession.class);
        if (data != null && data.contains("line")) {
            String rawLine = data.getStringTagValueOrThrow("line");
            String rawAuthor = data.getStringTagValueOrThrow("author");
            session.setCurrentLine(rawLine);
            session.setAuthor(rawAuthor);
        }
        switch (action) {
            case "close" -> {
                sessionManager.endSession(source);
                DialogPackets.clearDialog(source, DialogPackets.PacketPhase.PLAY);
            }
            case "retry" -> this.showTo(source, sessionManager, DialogPackets.PacketPhase.PLAY);
            case "add_line" -> {
                if (data == null) return;
                if (session.getCurrentLine().isBlank()) {
                    this.showRetry(source, "add_empty_line", session, sessionManager);
                    return;
                }
                session.getRawLines().add(session.getCurrentLine());
                session.setCurrentLine("");
                this.showTo(source, sessionManager, DialogPackets.PacketPhase.PLAY);
            }
            case "commit" -> {
                if ((useFallbackPermissions && !source.isNativeAdmin().toBooleanOrElse(false)) || (!useFallbackPermissions && !source.hasPermission(ServerChangelogs.NAMESPACE + ".command.create"))) {
                    sessionManager.endSession(source);
                    DialogPackets.showSimpleNotice(source, TITLE_KEY.apply(session), "dialog.editor.error.no_permission");
                    return;
                }
                if (data == null) return;
                if (session.getRawLines().isEmpty()) {
                    this.showRetry(source, "no_lines", session, sessionManager);
                    return;
                }
                try {
                    session.commit(this.storage);
                } catch (EditorSession.CommitException e) {
                    this.showRetry(source, session.getId() + "." + e.getTranslationKeyPart(), session, sessionManager);
                    return;
                }
                sessionManager.endSession(source);
                DialogPackets.showSimpleNotice(source, TITLE_KEY.apply(session), "dialog.editor." + session.getId() + ".success");
            }
        }
    }

    private void showRetry(@NotNull IPlayer source, @NotNull String errorPart, @NotNull EditorSession session, @NotNull DialogSessionManager sessionManager) {
        DialogPackets.showSimpleNotice(source, TITLE_KEY.apply(session), "dialog.editor.error." + errorPart, sessionManager.createSessionBasedAction(this, "retry", false), DialogPackets.PacketPhase.PLAY);
    }
}
