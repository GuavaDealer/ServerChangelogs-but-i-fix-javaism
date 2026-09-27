package com.codingcat.changelogs.velocity.player

import com.codingcat.changelogs.platformapi.player.PlatformPlayer
import com.codingcat.changelogs.platformapi.player.PlatformPlayerManager
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import java.util.*
import kotlin.jvm.optionals.getOrNull

object VelocityPlayerManager : PlatformPlayerManager {
    lateinit var server: ProxyServer
    override val onlinePlayers: ImmutableSet<PlatformPlayer>
        get() = server.allPlayers.map { fromNative(it) }.toImmutableSet()

    override fun getFromUUID(uuid: UUID): PlatformPlayer? {
        return server.getPlayer(uuid).getOrNull()?.let { fromNative(it) }
    }

    override fun getFromName(name: String): PlatformPlayer? {
        return server.getPlayer(name).getOrNull()?.let { fromNative(it) }
    }

    @Throws(IllegalArgumentException::class)
    override fun fromNative(nativePlayer: Any): PlatformPlayer {
        require(nativePlayer is Player) {
            "Expected native velocity player but got ${nativePlayer}"
        }
        return VelocityPlayerWrapper(nativePlayer)
    }
}
