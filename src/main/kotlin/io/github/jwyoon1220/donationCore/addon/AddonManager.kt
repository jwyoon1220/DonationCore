package io.github.jwyoon1220.donationCore.addon

import io.github.jwyoon1220.donationCore.DonationCore
import io.github.jwyoon1220.donationCore.addon.api.DonationCoreAddon
import io.github.jwyoon1220.donationCore.stream.Streamer.Companion.runMain
import org.bukkit.configuration.file.YamlConfiguration
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.jar.JarFile

class AddonManager {

    data class LoadedAddon(
        val addon: DonationCoreAddon,
        val classLoader: URLClassLoader
    )

    private val addons = mutableMapOf<String, LoadedAddon>()

    fun loadAddons(): CompletableFuture<Void> {
        return runAsync {
            val addonsDir = File(DonationCore.instance.dataFolder, "addons")
            if (!addonsDir.exists()) addonsDir.mkdirs()

            val jarFiles = addonsDir.listFiles { file ->
                file.isFile && file.name.endsWith(".jar")
            } ?: return@runAsync

            for (jar in jarFiles) {
                loadAddon(jar)
            }
        }
    }

    fun loadAddon(jarFile: File): CompletableFuture<Void>{
        return runAsync {
            if (!jarFile.exists() || !jarFile.name.endsWith(".jar")) return@runAsync

            DonationCore.instance.logger.info("Loading Addon ${jarFile.name}...")

            try {
                JarFile(jarFile).use { jar ->
                    val entry = jar.getJarEntry("addon.yml") ?: run {
                        DonationCore.instance.logger.warning("addon.yml not found in ${jarFile.name}")
                        return@runAsync
                    }
                    val configFile = jar.getJarEntry("config.yml") ?: run {
                        DonationCore.instance.logger.warning("config.yml not found in ${jarFile.name}")
                        return@runAsync
                    }

                    val yaml = Yaml()
                    val data = jar.getInputStream(entry).use { input ->
                        yaml.load<Map<String, Any>>(input)
                    }

                    val name = data["name"] as? String ?: run {
                        DonationCore.instance.logger.warning("name not defined in addon.yml (${jarFile.name})")
                        return@runAsync
                    }

                    val mainClassName = data["main"] as? String ?: run {
                        DonationCore.instance.logger.warning("main not defined in addon.yml ($name)")
                        return@runAsync
                    }

                    if (addons.containsKey(name)) {
                        DonationCore.instance.logger.warning("Addon $name is already loaded.")
                        return@runAsync
                    }

                    val classLoader = URLClassLoader(
                        arrayOf(jarFile.toURI().toURL()),
                        this.javaClass.classLoader
                    )

                    val clazz = classLoader.loadClass(mainClassName)
                    DonationCore.instance.logger.info("Loading addon ${mainClassName}...")

                    if (!DonationCoreAddon::class.java.isAssignableFrom(clazz)) {
                        DonationCore.instance.logger.warning("$mainClassName does not extend DonationCoreAddon")
                        classLoader.close()
                        return@runAsync
                    }

                    val addonDataFolder = File(DonationCore.instance.dataFolder, "addons/$name")
                    val targetFile = File(addonDataFolder, "config.yml")

                    if (!targetFile.exists()) targetFile.parentFile.mkdirs()

                    if (!targetFile.exists()) {
                        jar.getInputStream(configFile).use { input ->
                            Files.copy(
                                input,
                                targetFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            )
                        }
                    }

                    val addon = clazz.getDeclaredConstructor(String::class.java).newInstance(name) as DonationCoreAddon
                    runMain {
                        addon.onEnable()
                    }
                    addons[name] = LoadedAddon(addon, classLoader)
                    DonationCore.instance.logger.info("Addon loaded: $name (${addon.javaClass.simpleName})")
                }
            } catch (e: Exception) {
                DonationCore.instance.logger.severe("Failed to load addon ${jarFile.name}")
                e.printStackTrace()
            }
        }
    }

    fun unloadAddon(name: String): CompletableFuture<Void> {
        return runAsync {
            val loaded = addons[name] ?: return@runAsync
            try {
                runMain(loaded.addon::onDisable)
                loaded.classLoader.close()
                DonationCore.instance.logger.info("Addon unloaded: $name")
            } catch (e: Exception) {
                DonationCore.instance.logger.severe("Error while unloading addon $name")
                e.printStackTrace()
            }
            addons.remove(name)
        }
    }

    fun unloadAllAddons(): CompletableFuture<Void> {
        return runAsync {
            for ((name, loaded) in addons) {
                try {
                    runMain(loaded.addon::onDisable)
                    loaded.classLoader.close()
                    DonationCore.instance.logger.info("Addon unloaded: $name")
                } catch (e: Exception) {
                    DonationCore.instance.logger.severe("Error while unloading addon $name")
                    e.printStackTrace()
                }
            }
            addons.clear()
        }
    }

    fun getAddon(name: String): DonationCoreAddon? =
        addons[name]?.addon

    fun getAllAddons(): Map<String, DonationCoreAddon> =
        addons.mapValues { it.value.addon }
    fun getClassLoader(name: String): URLClassLoader? =
        addons[name]?.classLoader
    fun getAllClassLoaders(): Map<String, URLClassLoader> =
        addons.mapValues { it.value.classLoader }
}