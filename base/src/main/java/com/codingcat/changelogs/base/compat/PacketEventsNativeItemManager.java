package com.codingcat.changelogs.base.compat;

import com.codingcat.changelogs.platformapi.item.NativeItemManager;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketEventsNativeItemManager implements NativeItemManager {
    @Override
    public @NotNull ItemStack adaptToPEStack(@NotNull Object nativeStack) throws IllegalArgumentException {
        if (!(nativeStack instanceof ItemStack stack))
            throw new IllegalArgumentException("Expected PacketEvents ItemStack but got" + nativeStack);
        return stack;
    }

    @Override
    public @Nullable Object createNativeStack(@NotNull Key key, int amount) {
        ItemType type = ItemTypes.getRegistry().getByName(new ResourceLocation(key));
        return type != null ? ItemStack.builder().type(type).amount(amount).build() : null;
    }

    @Override
    public @NotNull Object applyComponentStr(@NotNull Object nativeStack, @NotNull String componentStr) {
        // TODO: Implement component parsing
        return nativeStack;
    }
}
