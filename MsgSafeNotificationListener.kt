package com.msgsafe.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.msgsafe.app.data.AppDatabase
import com.msgsafe.app.data.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MsgSafeNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Package names we care about. Add/remove as needed.
    private val trackedPackages = setOf(
        "com.facebook.orca",   // Messenger
        "com.facebook.mlite",  // Messenger Lite
        "com.whatsapp"         // WhatsApp (optional, same idea applies)
    )

    // Phrases apps use when a message is deleted/unsent, per package.
    // NOTE: these strings can change between app versions/languages — keep this list updated.
    private val deletionMarkers = listOf(
        "This message was deleted",
        "You deleted a message",
        "deleted a message",
        "This message was unsent"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val pkg = sbn.packageName
        if (pkg !in trackedPackages) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        val timestamp = sbn.postTime

        val isDeletionEvent = deletionMarkers.any { marker ->
            text.contains(marker, ignoreCase = true)
        }

        val dao = AppDatabase.getInstance(applicationContext).messageDao()

        if (isDeletionEvent) {
            // Try to find the most recent real message from this chat and mark it deleted,
            // instead of storing the "This message was deleted" placeholder itself.
            scope.launch {
                val candidate = dao.findLikelyDeletedCandidate(title, timestamp)
                if (candidate != null && !candidate.isDeleted) {
                    dao.update(
                        candidate.copy(
                            isDeleted = true,
                            deletedAtTimestamp = timestamp
                        )
                    )
                } else {
                    // No prior message on record — still log that a deletion happened,
                    // so the user at least knows *something* was removed.
                    dao.insert(
                        MessageEntity(
                            packageName = pkg,
                            chatName = title,
                            sender = title,
                            message = "[Original content not captured] $text",
                            timestamp = timestamp,
                            isDeleted = true,
                            deletedAtTimestamp = timestamp
                        )
                    )
                }
            }
        } else {
            // Normal incoming message — store it.
            scope.launch {
                dao.insert(
                    MessageEntity(
                        packageName = pkg,
                        chatName = title,
                        sender = title,
                        message = text,
                        timestamp = timestamp
                    )
                )
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Start the keep-alive foreground service so the OS is less likely
        // to kill the listener under battery optimization / Game Space.
        KeepAliveForegroundService.start(applicationContext)
    }
}
