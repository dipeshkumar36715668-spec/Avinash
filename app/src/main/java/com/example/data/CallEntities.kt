package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val phoneNumber: String,
    val callType: String, // "INCOMING", "OUTGOING", "MISSED"
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val status: String = "Online", // "Online", "Busy", "Offline"
    val avatarColorSeed: Int = 0, // Colorful profile background seed
    val personaSystemInstruction: String = "", // Prompt instructions for Gemini AI persona
    val isCustom: Boolean = false // If created by the user
)
