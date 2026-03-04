package io.github.jwyoon1220.donationCore.command

import io.github.jwyoon1220.donationCore.DonationCore
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil

class GetItemCommand : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}플레이어만 사용할 수 있습니다.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}사용법: /${label} <애드온이름:키값>")
            return true
        }

        // 입력값 분석 (예: "PrizeAddon:food")
        val input = args[0]
        val parts = input.split(":", limit = 2)

        if (parts.size < 2) {
            sender.sendMessage("${ChatColor.RED}형식이 올바르지 않습니다. (예: 애드온이름:아이템키)")
            return true
        }

        val addonName = parts[0]
        val itemPath = parts[1]

        // 1. 애드온 찾기
        val addon = DonationCore.addonManager.getAddon(addonName)
        if (addon == null) {
            sender.sendMessage("${ChatColor.RED}'${addonName}' 애드온을 찾을 수 없습니다.")
            return true
        }

        // 2. 아이템 가져오기
        val item = addon.config.get(itemPath) as? ItemStack
        if (item == null) {
            sender.sendMessage("${ChatColor.RED}'${itemPath}' 경로에 저장된 아이템이 없습니다.")
            return true
        }

        // 3. 지급
        sender.inventory.addItem(item.clone())
        sender.sendMessage("${ChatColor.GREEN}[${addonName}] ${ChatColor.WHITE}${itemPath} 아이템을 지급받았습니다.")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (args.size != 1) return emptyList()

        val completions = mutableListOf<String>()

        // 모든 애드온을 순회하며 ItemStack 타입인 키값만 수집
        for ((name, addon) in DonationCore.addonManager.getAllAddons()) {
            val config = addon.config
            // getKeys(true)를 사용하여 중첩된 경로(donation.food 등)까지 모두 탐색
            for (key in config.getKeys(true)) {
                if (config.get(key) is ItemStack) {
                    completions.add("$name:$key")
                }
            }
        }

        // 현재 입력 중인 텍스트와 매칭되는 항목 필터링
        return StringUtil.copyPartialMatches(args[0], completions, mutableListOf())
    }
}