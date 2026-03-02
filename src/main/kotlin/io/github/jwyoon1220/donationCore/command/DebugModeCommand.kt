package io.github.jwyoon1220.donationCore.command

import com.benasher44.uuid.UUID
import io.github.jwyoon1220.donationCore.command.DebugModeCommand.dbgMap
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object DebugModeCommand: CommandExecutor {

    val dbgMap = HashMap<java.util.UUID, Boolean>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>?): Boolean {
        if (!sender.isOp) return true

        if (args.isNullOrEmpty()) return true

        if (sender !is Player) return true

        dbgMap[sender.uniqueId] = when (args[0]?.lowercase()) {
            "true" -> true
            "false" -> false
            else -> false
        }
        sender.sendMessage("${ChatColor.GREEN} dbg:${sender.playerListName}=${dbgMap[sender.uniqueId]}")
        return true
    }

    inline fun runIfDebugMode(player: Player, block: () -> Unit) {
        if (dbgMap[player.uniqueId] == true) {
            block()
        }
    }

}