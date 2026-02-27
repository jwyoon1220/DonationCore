package io.github.jwyoon1220.donationCore

import io.github.jwyoon1220.donationCore.addon.AddonManager
import io.github.jwyoon1220.donationCore.command.ConnectCommand
import io.github.jwyoon1220.donationCore.command.DonationTestCommand
import io.github.jwyoon1220.donationCore.command.ReloadAllConfigCommand
import io.github.jwyoon1220.donationCore.command.SetConfigCommand
import io.github.jwyoon1220.donationCore.stream.DonationManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.ChzzkClient
import xyz.r2turntrue.chzzk4j.ChzzkClientBuilder
import zzik2.soop4j.SoopClient

class DonationCore : JavaPlugin() {

    companion object {
        val CHZZK_CLIENT: ChzzkClient = ChzzkClientBuilder().build()
        val SOOP_CLIENT: SoopClient = SoopClient.Builder().build()

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
        getCommand("setconfig")?.setExecutor(SetConfigCommand(config))
        getCommand("reloadconfig")?.setExecutor(ReloadAllConfigCommand())
        saveDefaultConfig()

        addonManager.loadAddons()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        addonManager.unloadAllAddons()
        saveConfig()
    }
}
