package com.msgsafe.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msgsafe.app.data.MessageEntity
import com.msgsafe.app.util.PermissionHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MsgSafeApp()
            }
        }
    }
}

@Composable
fun MsgSafeApp(viewModel: MessageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    var notifGranted by remember { mutableStateOf(PermissionHelper.isNotificationAccessGranted(context)) }
    var batteryIgnored by remember { mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context)) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MsgSafe") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            if (!notifGranted || !batteryIgnored) {
                SetupChecklist(
                    notifGranted = notifGranted,
                    batteryIgnored = batteryIgnored,
                    onGrantNotifClick = { PermissionHelper.openNotificationAccessSettings(context) },
                    onGrantBatteryClick = { PermissionHelper.requestIgnoreBatteryOptimizations(context) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchChanged(it) },
                label = { Text("Search by name or message") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(messages) { msg ->
                    MessageRow(msg)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SetupChecklist(
    notifGranted: Boolean,
    batteryIgnored: Boolean,
    onGrantNotifClick: () -> Unit,
    onGrantBatteryClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Setup needed", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (!notifGranted) {
                Text("1. Allow Notification Access so MsgSafe can catch messages.")
                Button(onClick = onGrantNotifClick) { Text("Grant Notification Access") }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!batteryIgnored) {
                Text("2. Disable battery optimization so background catching isn't killed.")
                Button(onClick = onGrantBatteryClick) { Text("Disable Battery Optimization") }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                "Note: If Messenger is used inside Game Space / Game Booster, " +
                        "you also need to allow notifications for it there — this can't be set from MsgSafe.",
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun MessageRow(msg: MessageEntity) {
    val fmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(msg.chatName, fontWeight = FontWeight.Bold)
            Text(fmt.format(Date(msg.timestamp)), style = MaterialTheme.typography.bodySmall)
        }
        Text(msg.message)
        if (msg.isDeleted) {
            Text(
                "⚠️ ${msg.chatName} deleted this message",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
