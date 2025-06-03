package edu.stanford.bdh.engagehf.modules.bluetooth.model

import kotlinx.serialization.Serializable

@Serializable
data class BLEDevice(
    val address: String,
    val name: String,
    val connected: Boolean,
    val lastSeenTimeStamp: Long = 0L,
)
