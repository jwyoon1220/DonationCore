package io.github.jwyoon1220.donationCore.command

import io.github.jwyoon1220.donationCore.DonationCore
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class SetConfigCommand(private val config: FileConfiguration): CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (args.isEmpty()) return false
        if (sender !is Player) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.")
            return true
        }

        for ((name, addon) in DonationCore.addonManager.getAllAddons()) {
            val addr = args[0]
            addon.config.set(addr, sender.inventory.itemInMainHand)
            sender.sendMessage("${name}: ${addr}=${sender.inventory.itemInMainHand.type}")
            addon.saveConfig()
            return true
        }
        return true
    }
}