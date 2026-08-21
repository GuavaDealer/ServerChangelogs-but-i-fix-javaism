package com.codingcat.changelogs.base.dialog;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.NoticeDialog;
import com.github.retrooper.packetevents.protocol.dialog.action.Action;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerClearDialog;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerShowDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerClearDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.codingcat.changelogs.base.lang.TranslationSource.translatableManual;

public final class DialogPackets {
    public static void showDialog(@NotNull IPlayer player, @NotNull Dialog dialog, @NotNull PacketPhase packetPhase) {
        packet(player, packetPhase == PacketPhase.CONFIGURATION ? new WrapperConfigServerShowDialog(dialog) : new WrapperPlayServerShowDialog(dialog));
    }

    public static void showSimpleNotice(@NotNull IPlayer player, @NotNull String titleTranslation, @NotNull String bodyTranslation) {
        showSimpleNotice(player, titleTranslation, bodyTranslation, null, PacketPhase.PLAY);
    }

    public static void showSimpleNotice(@NotNull IPlayer player, @NotNull String titleTranslation, @NotNull String bodyTranslation, @Nullable Action action, @NotNull PacketPhase packetPhase) {
        DialogBody body = new PlainMessageDialogBody(new PlainMessage(translatableManual(player, bodyTranslation), 400));
        CommonDialogData common = new CommonDialogData(
                translatableManual(player, titleTranslation),
                null, true, false,
                action != null ? DialogAction.NONE : DialogAction.CLOSE, List.of(body), List.of()
        );
        ActionButton closeButton = new ActionButton(new CommonButtonData(translatableManual(player, "dialog.popup.close"), null, 40), action);
        showDialog(player, new NoticeDialog(common, closeButton), packetPhase);
    }

    public static void clearDialog(@NotNull IPlayer player, @NotNull PacketPhase packetPhase) {
        packet(player, packetPhase == PacketPhase.CONFIGURATION ? new WrapperConfigServerClearDialog() : new WrapperPlayServerClearDialog());
    }

    private static void packet(@NotNull IPlayer player, @NotNull PacketWrapper<?> wrapper) {
        User user = (User) player.asPacketEventsUser();
        user.sendPacket(wrapper);
    }

    public enum PacketPhase {
        CONFIGURATION,
        PLAY
    }
}
