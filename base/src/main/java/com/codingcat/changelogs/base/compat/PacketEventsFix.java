package com.codingcat.changelogs.base.compat;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.PatchableComponentMap;
import com.github.retrooper.packetevents.protocol.component.builtin.item.FoodProperties;
import com.github.retrooper.packetevents.protocol.dialog.body.ItemDialogBody;
import com.github.retrooper.packetevents.util.PEVersion;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * This class introduces fixes for versions of PacketEvents that cause problems with the dialog system.
 */
public final class PacketEventsFix {
    private static @NotNull Set<Workaround> enabledWorkarounds = new HashSet<>();

    @RequiredArgsConstructor
    public enum Workaround {
        // https://github.com/retrooper/packetevents/issues/1569 - until v2.13.0 on MC versions < 26.1
        INVALID_ITEM_BODY_ENCODING((p, s) -> p.isOlderThan(new PEVersion(2, 13, 1)) && s.isOlderThan(ServerVersion.V_26_1));

        private final @NotNull BiPredicate<PEVersion, ServerVersion> versionPredicate;

        public boolean isAffected() {
            PEVersion peVersion = PacketEvents.getAPI().getVersion();
            ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
            return this.versionPredicate.test(peVersion, serverVersion);
        }
    }

    public static void checkAndLoad(@NotNull ComponentLogger logger) {
        enabledWorkarounds = Arrays.stream(Workaround.values())
                .filter(Workaround::isAffected)
                .collect(HashSet::new, Set::add, HashSet::addAll);
        if (!enabledWorkarounds.isEmpty()) {
            logger.warn("You are using a version of PacketEvents that contains known problems. The plugin will automatically work around those issues, but updating to the latest version is recommended!");
            List<String> workaroundNames = enabledWorkarounds.stream().map(Enum::name).toList();
            logger.warn("Enabled workarounds: {}", String.join(", ", workaroundNames));
        }
    }

    public static boolean isEnabled(@NotNull Workaround workaround) {
        return enabledWorkarounds.contains(workaround);
    }

    @SuppressWarnings("deprecation")
    public static void fixItemBody(@NotNull ItemDialogBody itemBody) {
        if (!isEnabled(Workaround.INVALID_ITEM_BODY_ENCODING)) return;
        PatchableComponentMap componentMap = itemBody.getItem().getComponents();
        if (!componentMap.getPatches().isEmpty()) return;
        // Add dummy component data which won't affect the item visually
        componentMap.set(ComponentTypes.FOOD, new FoodProperties(123, 123f, false));
    }
}
