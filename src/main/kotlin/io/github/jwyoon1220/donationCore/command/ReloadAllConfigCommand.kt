package io.github.jwyoon1220.donationCore.command

import io.github.jwyoon1220.donationCore.DonationCore
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadAllConfigCommand: CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>?
    ): Boolean {

        if (!sender.isOp) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to do this command!")
            return true
        }

        for ((name, addon) in DonationCore.addonManager.getAllAddons()) {
            addon.reloadConfig()
            sender.sendMessage("${ChatColor.GREEN} $name: Config Reloaded.")
        }
        return true
    }
}