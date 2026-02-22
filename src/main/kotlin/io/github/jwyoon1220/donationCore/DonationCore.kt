package io.github.jwyoon1220.donationCore

import io.github.jwyoon1220.donationCore.addon.AddonManager
import io.github.jwyoon1220.donationCore.command.ConnectCommand
import io.github.jwyoon1220.donationCore.command.DonationTestCommand
import io.github.jwyoon1220.donationCore.stream.DonationManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.ChzzkClientBuilder

class DonationCore : JavaPlugin() {

    companion object {
        val CHZZK_CLIENT = ChzzkClientBuilder().build()
        lateinit var instance: DonationCore
        val manager = DonationManager()
        val addonManager = AddonManager()
    }

    override fun onEnable() {
        // Plugin startup logic
        instance = this
        server.pluginManager.registerEvents(GlobalEventListener, this)

        getCommand("donationcore")?.setExecutor(ConnectCommand())
        getCommand("donationtest")?.setExecutor(DonationTestCommand())
        saveDefaultConfig()

        addonManager.loadAddons()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        addonManager.unloadAllAddons()
        saveConfig()
    }
}
