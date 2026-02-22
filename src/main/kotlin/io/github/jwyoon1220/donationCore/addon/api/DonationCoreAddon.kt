package io.github.jwyoon1220.donationCore.addon.api

import io.github.jwyoon1220.donationCore.DonationCore
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Logger

abstract class DonationCoreAddon(private val name: String) {

    val logger: Logger = Logger.getLogger(javaClass.simpleName)

    val dcPlugin = DonationCore.instance
    val manager = DonationCore.manager
    val server = dcPlugin.server

    // ✅ 애드온 전용 폴더
    val dataFolder: File = File(dcPlugin.dataFolder, "addons/$name")

    private var configFile: File = File(dataFolder, "config.yml")
    var config: FileConfiguration

    init {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        if (!configFile.exists()) {
            saveDefaultConfig()
        }

        config = YamlConfiguration.loadConfiguration(configFile)
    }

    abstract fun onEnable()

    abstract fun onDisable()

    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun saveConfig() {
        config.save(configFile)
    }

    fun saveDefaultConfig() {
        configFile.parentFile.mkdirs()
        configFile.createNewFile()
    }
}