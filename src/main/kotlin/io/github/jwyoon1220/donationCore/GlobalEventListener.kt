package io.github.jwyoon1220.donationCore

import io.github.jwyoon1220.donationCore.stream.Platform
import io.github.jwyoon1220.donationCore.stream.Streamer
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.UUID

object GlobalEventListener: Listener {
    val streamers = HashMap<UUID, Streamer>()

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val config  = DonationCore.instance.config

        val id = config.getString("${player.uniqueId}.id")
        val platform = try {
            Platform.valueOf(config.getString("${player.uniqueId}.platform") ?: return)
        } catch (e: IllegalArgumentException) {
            player.sendMessage("§8[§6DonationCore§8] §c저장된 플랫폼 정보가 올바르지 않습니다.")
            player.sendMessage("§7다시 설정해주세요: §e/donationcore|dc <플랫폼> <아이디>")
            return
        }
        if (id == null) {
            player.sendMessage("§8[§6DonationCore§8] §e스트리밍 계정이 설정되지 않았습니다.")
            player.sendMessage("§7설정 방법: §e/donationcore|dc <플랫폼> <아이디>")
            return
        } else {
            val streamer = Streamer(player, id, platform)
            streamer.connect()
            streamers[player.uniqueId] = streamer
        }

    }
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        streamers[event.player.uniqueId]?.disconnect()
        streamers.remove(event.player.uniqueId)
    }
}