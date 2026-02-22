package io.github.jwyoon1220.donationCore.stream

import io.github.jwyoon1220.donationCore.DonationCore
import io.github.jwyoon1220.donationCore.addon.api.StreamListener
import org.bukkit.OfflinePlayer

class DonationManager {

    private val listeners = mutableListOf<StreamListener>()

    fun addGlobalListener(listener: StreamListener) {
        listeners.add(listener)
    }
    fun removeGlobalListener(listener: StreamListener) {
        listeners.remove(listener)
    }

    fun notifyGlobalListeners(streamer: Streamer, platform: Platform, type: DonationType, profile: Streamer.Donation) {
        for (listener in listeners) {
            listener.onDonation(streamer, platform, type, profile)
        }
    }
}