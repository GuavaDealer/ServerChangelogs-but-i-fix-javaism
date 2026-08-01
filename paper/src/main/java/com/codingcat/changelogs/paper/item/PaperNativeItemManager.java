package com.codingcat.changelogs.paper.item;

import com.codingcat.changelogs.platformapi.item.NativeItemManager;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperNativeItemManager implements NativeItemManager {
    @Override
    public @NotNull Object adaptToPEStack(@NotNull Object nativeStack) throws IllegalArgumentException {
        if (!(nativeStack instanceof ItemStack itemStack))
            throw new IllegalArgumentException("Expected native bukkit ItemStack but got " + nativeStack);
        return SpigotConversionUtil.fromBukkitItemStack(itemStack);
    }

    @Override
    public @Nullable Object createNativeStack(@NotNull Key key, int amount) {
        ItemType type = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).get(key);
        return type != null ? type.createItemStack(amount) : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull Object applyComponentStr(@NotNull Object nativeStack, @NotNull String componentStr) throws IllegalArgumentException {
        if (!(nativeStack instanceof ItemStack itemStack))
            throw new IllegalArgumentException("Expected native bukkit ItemStack but got " + nativeStack);
        return Bukkit.getUnsafe().modifyItemStack(itemStack, componentStr);
    }
}
