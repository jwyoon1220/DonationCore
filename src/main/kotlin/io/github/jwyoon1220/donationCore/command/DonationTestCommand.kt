package io.github.jwyoon1220.donationCore.command

import io.github.jwyoon1220.donationCore.DonationCore
import io.github.jwyoon1220.donationCore.GlobalEventListener
import io.github.jwyoon1220.donationCore.stream.DonationType
import io.github.jwyoon1220.donationCore.stream.Platform
import io.github.jwyoon1220.donationCore.stream.Streamer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DonationTestCommand: CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (args.size < 2) {
            sender.sendMessage("사용법: /donationtest|dt <닉네임> <금액>")
            return true
        }

        val nickname = args[0]
        val amount = args[1].toIntOrNull()
        if (amount == null) {
            sender.sendMessage("${ChatColor.RED}금액은 숫자로 입력해야 합니다.")
            return true
        }

        val target = Bukkit.getPlayer(nickname) ?: run {
            sender.sendMessage("${ChatColor.RED}플레이어 '$nickname'을(를) 찾을 수 없습니다.")
            return true
        }

        val streamer = GlobalEventListener.streamers[target.uniqueId] ?: run {
            sender.sendMessage("${ChatColor.RED}플레이어 '$nickname'은(는) 스트리머로 등록되어 있지 않습니다.")
            return true
        }

        DonationCore.manager.notifyGlobalListeners(streamer, Platform.SOOP,
            DonationType.NORMAL, Streamer.Donation(
                id = "AFREECA_DONATION",
                nickname = "테스트 기부자",
                payAmount = amount,
                type = "NORMAL",
                message = "테스트 기부 메시지"
            ))
        sender.sendMessage("${ChatColor.GREEN}테스트 기부 이벤트가 발생했습니다: $nickname -> $amount")
        return true
    }
}