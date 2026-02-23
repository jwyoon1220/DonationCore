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
import java.util.concurrent.TimeUnit

class Streamer(
    val player: OfflinePlayer,
    private val id: String,
    private val platform: Platform
) {

    private lateinit var chzzkChat: ChzzkChat

    private val listeners = ArrayList<StreamListener>()
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()
    private var ws: WebSocket? = null
    private val gson = Gson()

    companion object {
        const val SOOP_URL = "ws://streamer.biancaapi.com?platformId=afreeca&bjId="
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
        chzzkChat = ChzzkChatBuilder(DonationCore.CHZZK_CLIENT, id).build()

        chzzkChat.on(NormalDonationEvent::class.java) { event ->

            val payAmount = event?.message?.payAmount ?: 0
            val donation = Donation(
                type = "CHZZK_DONATION",
                nickname = event?.message?.profile?.nickname ?: "익명",
                message = event?.message?.content ?: "메시지 없음",
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
        } catch (e: Exception) {
            e.printStackTrace()
            player.player?.sendTranslatedMessage("&c치지직 연결중 오류가 발생했습니다. 채널 ID를 확인해주세요.")
            return
        }

        player.player?.sendTranslatedMessage("&a치지직에 연결되었습니다. (ChID: $id, name: ${DonationCore.CHZZK_CLIENT.fetchChannel(id).channelName})")
    }

    private fun connectToSOOP() {
        val request = Request.Builder().url(SOOP_URL + id).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                runMain {
                    player.player?.sendTranslatedMessage("&aSOOP에 연결되었습니다. &7(BJID: &e$id&7)")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
               try {
                   val donation = gson.fromJson(text, Donation::class.java)
                   if (donation.type == "AFREECA_DONATION") {
                       runMain {
                           notifyDonation(
                               platform = Platform.SOOP,
                               type = DonationType.NORMAL,
                               profile = donation,
                           )
                       }
                   }
               } catch (e: Exception) {
                   e.printStackTrace()
               }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
                runMain {
                    player.player?.sendTranslatedMessage("SOOP에 연결중 오류가 발생했습니다. BJID가 올바른지 확인해주세요.")
                }
            }
        })
    }

    /** 후원 이벤트 전달 */
    fun notifyDonation(platform: Platform, type: DonationType, profile: Donation) {
        val unit = when (platform) {
            Platform.SOOP -> "별붕선"
            Platform.CHZZK -> "치즈"
        }
        //player.player?.sendMessage("&a새로운 ${unit}이(가) 도착했습니다! (${profile.nickname}: &7${profile.payAmount}&a${unit}): &7${profile.nickname}: ${profile.message ?: ""}")
        listeners.forEach { it.onDonation(this, platform, type, profile) }
        DonationCore.manager.notifyGlobalListeners(this, platform, type, profile)
        //player.player?.sendMessage("ntfy")
    }

    fun disconnect() {
        when (platform) {
            Platform.SOOP -> ws?.close(1000, "Player disconnected")
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

    fun Player.sendActionBar(message: String) {
        this.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent(ChatColor.translateAlternateColorCodes('&', message))
        )
    }
    fun Player.sendTranslatedMessage(message: String) {
        this.sendMessage(ChatColor.translateAlternateColorCodes('&', message))
    }
}