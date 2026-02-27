package io.github.jwyoon1220.donationCore.stream

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.jwyoon1220.donationCore.DonationCore
import io.github.jwyoon1220.donationCore.addon.api.StreamListener
import jdk.internal.net.http.common.Log.channel
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import okhttp3.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import xyz.r2turntrue.chzzk4j.chat.ChzzkChatBuilder
import xyz.r2turntrue.chzzk4j.chat.event.MissionDonationEvent
import xyz.r2turntrue.chzzk4j.chat.event.NormalDonationEvent
import zzik2.soop4j.api.SoopChannel
import zzik2.soop4j.chat.SoopChat
import zzik2.soop4j.chat.SoopChatListener
import zzik2.soop4j.chat.event.ConnectEvent
import zzik2.soop4j.chat.event.DonationEvent
import zzik2.soop4j.chat.event.EmoticonEvent
import zzik2.soop4j.chat.event.NotificationEvent
import zzik2.soop4j.model.channel.StationInfo
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class Streamer(
    val player: OfflinePlayer,
    private val id: String,
    private val platform: Platform
) {

    private val logger = Logger.getLogger("Streamer(id: $id)")

    private lateinit var chzzkChat: ChzzkChat
    private lateinit var soopChat: SoopChat

    private val listeners = ArrayList<StreamListener>()

    private var isFirstConnect = true

    companion object {
        inline fun runMain(crossinline block: () -> Unit) {
            if (Bukkit.isPrimaryThread()) {
                block()
            } else {
                Bukkit.getScheduler().runTask(DonationCore.instance, Runnable {
                    block()
                })
            }
        }
    }

    fun addListener(listener: StreamListener) { listeners.add(listener) }
    fun removeListener(listener: StreamListener) { listeners.remove(listener) }

    fun connect() {
        when (platform) {
            Platform.SOOP -> connectToSOOP()
            Platform.CHZZK -> connectToChzzk()
        }
    }

    private fun connectToChzzk() {
        chzzkChat = ChzzkChatBuilder(DonationCore.CHZZK_CLIENT, id).withAutoReconnect(true).build()

        chzzkChat.on(NormalDonationEvent::class.java) { event ->

            val payAmount = event?.message?.payAmount ?: 0
            val donation = Donation(
                type = "CHZZK_DONATION",
                nickname = event?.message?.profile?.nickname ?: "익명",
                message = event?.message?.content ?: "익명",
                payAmount = payAmount,
                id = this.id
            )
            notifyDonation(Platform.CHZZK, DonationType.NORMAL, donation)
        }
        chzzkChat.on(MissionDonationEvent::class.java) { event ->

            val payAmount = event?.message?.payAmount ?: 0
            val donation = Donation(
                type = "CHZZK_DONATION",
                nickname = event?.message?.profile?.nickname ?: "Unknown",
                message = event?.message?.content ?: "Unknown",
                payAmount = payAmount,
                id = this.id
            )
            notifyDonation(Platform.CHZZK, DonationType.MISSON, donation)
        }

        try {
            chzzkChat.connectAsync()
            logger.info("${player.name}: Connecting to chzzk... (id: $id)")
        } catch (e: Exception) {
            logger.info("${player.name}: Exception on Connection ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            player.player?.sendTranslatedMessage("&c치지직 연결중 오류가 발생했습니다. (${e.javaClass.name}: ${e.message})")
            player.player?.sendTranslatedMessage("&c StackTrace: ${e.stackTraceToString()}")
            return
        }
        logger.info("${player.name}: Connected to chzzk.")

        player.player?.sendTranslatedMessage("&a치지직에 연결되었습니다. (ChID: $id, name: ${DonationCore.CHZZK_CLIENT.fetchChannel(id).channelName})")
    }

    private fun connectToSOOP() {
        soopChat = DonationCore.SOOP_CLIENT.chat(id)
           .autoReconnect(true)
           .reconnectDelayMs(5000)
           .maxReconnectAttempts(5)
           .build();
        val stationInfo = DonationCore.SOOP_CLIENT.channel().station(id)
        val name = stationInfo.station.stationName

        soopChat.addListener(object : SoopChatListener {
            override fun onDonation(event: DonationEvent) {
                val donation = Donation(
                    type = "SOOP_DONATION",
                    nickname = event.fromUsername ?: event.from ?: "Unknown",
                    message = "", // TODO: SOOP은 별풍선, 애드벌룬 이벤트에 메시지가 있지 않고 TTS가 이 이벤트 후 바로 오는 채팅 이벤트를 출력.
                    payAmount = event.amount,
                    id = this@Streamer.id
                )
                notifyDonation(Platform.SOOP, DonationType.NORMAL, donation)
            }
        })
        try {
            soopChat.connectAsync()
            logger.info("${player.name}: Connecting to soop...")
        } catch (e: Exception) {
            logger.info("${player.name}: Exception on Connection ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            player()?.sendTranslatedMessage("&cSOOP 연결중 오류가 발생했습니다. (${e.javaClass.name}: ${e.message})")
            player()?.sendTranslatedMessage("&cStackTrace: ${e.stackTraceToString()}")
            return
        }
        logger.info("${player.name}: Connected to soop.")
        player()?.sendTranslatedMessage("SOOP에 연결되었습니다. (BJID: $id, name: $name)")
    }

    /** 후원 이벤트 전달 */
    fun notifyDonation(platform: Platform, type: DonationType, profile: Donation) {
        listeners.forEach { it.onDonation(this, platform, type, profile) }
        DonationCore.manager.notifyGlobalListeners(this, platform, type, profile)
    }

    fun disconnect() {
        when (platform) {
            Platform.SOOP -> soopChat.disconnect()
            Platform.CHZZK -> chzzkChat.closeAsync()
        }
    }

    data class Donation(
        val type: String,
        val nickname: String,
        val message: String?,
        val payAmount: Int,
        val id: String
    )

    fun player(): Player? {
        return player.player
    }

    fun Player.sendActionBar(message: String) {
        this.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent(ChatColor.translateAlternateColorCodes('&', message))
        )
    }
    fun Player.sendTranslatedMessage(message: String) {
        this.sendMessage(ChatColor.translateAlternateColorCodes('&', message))
        this.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent(ChatColor.translateAlternateColorCodes('&', message))
        )
    }
}