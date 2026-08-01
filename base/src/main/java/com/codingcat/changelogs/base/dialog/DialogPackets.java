package com.codingcat.changelogs.base.dialog;

import com.codingcat.changelogs.platformapi.player.IPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerClearDialog;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerShowDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerClearDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import org.jetbrains.annotations.NotNull;

public final class DialogPackets {
    public static void showDialog(@NotNull IPlayer player, @NotNull Dialog dialog, @NotNull PacketPhase packetPhase) {
        packet(player, packetPhase == PacketPhase.CONFIGURATION ? new WrapperConfigServerShowDialog(dialog) : new WrapperPlayServerShowDialog(dialog));
    }

    public static void clearDialog(@NotNull IPlayer player, @NotNull PacketPhase packetPhase) {
        packet(player, packetPhase == PacketPhase.CONFIGURATION ? new WrapperConfigServerClearDialog() : new WrapperPlayServerClearDialog());
    }

    private static void packet(@NotNull IPlayer player, @NotNull PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player.unwrapNative(), wrapper);
    }

    public enum PacketPhase {
        CONFIGURATION,
        PLAY
    }
}
