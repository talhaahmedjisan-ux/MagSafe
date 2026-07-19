package com.msgsafe.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,      // e.g. com.facebook.orca (Messenger)
    val chatName: String,         // group/person name shown in notification title
    val sender: String,           // best-guess sender (title, sometimes same as chatName)
    val message: String,          // notification text content
    val timestamp: Long,          // epoch millis when captured
    val isDeleted: Boolean = false,
    val deletedAtTimestamp: Long? = null
)
