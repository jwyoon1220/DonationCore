package io.github.jwyoon1220.donationCore.addon.api

import io.github.jwyoon1220.donationCore.stream.DonationType
import io.github.jwyoon1220.donationCore.stream.Platform
import io.github.jwyoon1220.donationCore.stream.Streamer
import org.bukkit.OfflinePlayer

interface StreamListener {

    fun onDonation(streamer: Streamer, platform: Platform, type: DonationType, profile: Streamer.Donation)

}