package io.github.jwyoon1220.donationCore.command

import io.github.jwyoon1220.donationCore.DonationCore
import io.github.jwyoon1220.donationCore.GlobalEventListener
import io.github.jwyoon1220.donationCore.stream.Platform
import io.github.jwyoon1220.donationCore.stream.Streamer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ConnectCommand : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.")
            return true
        }

        if (args.size < 2) {
            sender.sendMessage("/donationcore <플랫폼> <아이디>")
            return true
        }

        val platformInput = args[0].uppercase()
        val id = args[1]

        val platform = try {
            Platform.valueOf(platformInput)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("알 수 없는 플랫폼: $platformInput")
            sender.sendMessage("사용 가능한 플랫폼: ${Platform.entries.joinToString(", ") { it.name }}")
            return true
        }

        // 기존에 있으면 제거하고 새로 등록
        val existing = GlobalEventListener.streamers[sender.uniqueId]
        existing?.disconnect() // 기존 연결 종료

        val streamer = Streamer(sender, id, platform)
        GlobalEventListener.streamers[sender.uniqueId] = streamer
        sender.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("${ChatColor.GREEN}[${platform.name}] ${ChatColor.YELLOW}${id}로 연결 시도 중..."))
        streamer.connect()

        val config = DonationCore.instance.config
        config.set("${sender.uniqueId}.id", id)
        config.set("${sender.uniqueId}.platform", platform.name)
        DonationCore.instance.saveConfig()

        return true
    }

    // 탭 컴플리트
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val completions = mutableListOf<String>()
        if (args.size == 1) {
            // 첫 번째 인자는 플랫폼
            val prefix = args[0].uppercase()
            completions.addAll(Platform.entries.map { it.name }.filter { it.startsWith(prefix) })
        }
        return completions
    }
}