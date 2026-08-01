package com.codingcat.changelogs.base.dialog.ui;

import com.codingcat.changelogs.base.data.ChangelogEntry;
import com.codingcat.changelogs.base.data.ChangelogStorage;
import com.codingcat.changelogs.base.dialog.DialogSessionManager;
import com.codingcat.changelogs.base.dialog.IDialog;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.NoticeDialog;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.ItemDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.codingcat.changelogs.base.lang.TranslationSource.translatable;
import static com.codingcat.changelogs.base.lang.TranslationSource.translatableManual;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public class ChangelogDialog implements IDialog {
    private final @Getter String id = "changelog_view";
    private final @NotNull ChangelogStorage storage;
    private final @NotNull DateTimeFormatter dateFormatter;
    private final boolean addHeader;
    private final @Nullable ItemStack headerItem;

    @Override
    public @NotNull Dialog build(@NotNull IPlayer p, @NotNull DialogSessionManager sessionManager) {
        Component authorNull = translatableManual(p, "dialog.changelog.unspecified_author");
        List<DialogBody> body = this.storage.listEntries()
                .reversed().stream()
                .map(e -> translatableManual(p, "dialog.changelog.entry" + (!e.hasRead(p) ? "_unread" : ""),
                        text(dateFormatter.format(e.recordedAt())), createLinesComponent(p, e), Objects.requireNonNullElse(e.author(), authorNull)))
                .map(c -> (DialogBody) new PlainMessageDialogBody(new PlainMessage(c, 440)))
                .toList();
        if (body.isEmpty())
            body = List.of(new PlainMessageDialogBody(new PlainMessage(translatableManual(p, "dialog.changelog.empty"), 350)));
        if (this.addHeader) {
            body = new ArrayList<>(body);
            PlainMessage headerMessage = new PlainMessage(translatableManual(p, "dialog.changelog.header"), 260);
            body.addFirst(headerItem != null ? new ItemDialogBody(headerItem, headerMessage, false, false, 17, 17) : new PlainMessageDialogBody(headerMessage));
        }
        CommonDialogData common = new CommonDialogData(
                translatableManual(p, "dialog.changelog.title"),
                null, true, false,
                DialogAction.CLOSE, body, List.of()
        );
        ActionButton button = new ActionButton(new CommonButtonData(
                translatableManual(p, "dialog.changelog.button.close"),
                null, 60
        ), sessionManager.createStatic(this, "confirm_read"));
        return new NoticeDialog(common, button);
    }

    private @NotNull Component createLinesComponent(@NotNull IPlayer player, @NotNull ChangelogEntry entry) {
        Component component = Component.empty();
        List<Component> newLines = entry.lines().stream()
                .map(l -> translatableManual(player, "dialog.changelog.entry_line", l))
                .toList();
        for (Component line : newLines) {
            component = component.append(line);
            if (newLines.indexOf(line) < newLines.size() - 1) component = component.appendNewline();
        }
        return component;
    }

    @Override
    public void onActionTriggered(@NotNull String action, @Nullable NBTCompound data, @NotNull IPlayer source, @NotNull DialogSessionManager sessionManager) {
        if (!action.equals("confirm_read")) return;
        List<Integer> uids = this.storage.listEntries()
                .stream()
                .filter(e -> !e.hasRead(source))
                .map(ChangelogEntry::uid)
                .toList();
        uids.forEach(uid -> storage.markAsRead(uid, source.getUniqueId()));
        if (!uids.isEmpty()) source.asAudience().sendMessage(translatable("dialog.changelog.read", text(uids.size())));
    }
}
