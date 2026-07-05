package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CallLogEntity
import com.example.data.ContactEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Premium aesthetic dark palettes for calling
val SlateBg = Color(0xFF0F0E17)
val CardBg = Color(0xFF1F1D2B)
val DeepIndigo = Color(0xFF2D2A4A)
val BrightCyan = Color(0xFF00F0FF)
val BrightPink = Color(0xFFFA26A0)
val PhoneGreen = Color(0xFF00D1FF) // Digital Cyan Phone call
val ActiveGreen = Color(0xFF4E9F3D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCallingApp(viewModel: CallViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeCallState by viewModel.activeCallState.collectAsStateWithLifecycle()

    var showAddContactDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_layout"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BrightCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FREE ONLINE CALL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SlateBg
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == ActiveTab.DIALER,
                    onClick = { viewModel.selectTab(ActiveTab.DIALER) },
                    icon = { Icon(Icons.Default.Dialpad, contentDescription = "Dialer") },
                    label = { Text("Dialer", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrightCyan,
                        selectedTextColor = BrightCyan,
                        indicatorColor = DeepIndigo,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_dialer")
                )
                NavigationBarItem(
                    selected = selectedTab == ActiveTab.CONTACTS,
                    onClick = { viewModel.selectTab(ActiveTab.CONTACTS) },
                    icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                    label = { Text("Contacts", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrightCyan,
                        selectedTextColor = BrightCyan,
                        indicatorColor = DeepIndigo,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_contacts")
                )
                NavigationBarItem(
                    selected = selectedTab == ActiveTab.HISTORY,
                    onClick = { viewModel.selectTab(ActiveTab.HISTORY) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("Logs", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrightCyan,
                        selectedTextColor = BrightCyan,
                        indicatorColor = DeepIndigo,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_history")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == ActiveTab.CONTACTS) {
                FloatingActionButton(
                    onClick = { showAddContactDialog = true },
                    containerColor = BrightCyan,
                    contentColor = SlateBg,
                    modifier = Modifier.testTag("add_contact_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Persona Contact")
                }
            }
        },
        containerColor = SlateBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ActiveTab.DIALER -> DialerScreen(viewModel)
                ActiveTab.CONTACTS -> ContactsScreen(viewModel)
                ActiveTab.HISTORY -> CallHistoryScreen(viewModel)
            }

            // High-fidelity Full Screen Call Overlay
            AnimatedVisibility(
                visible = activeCallState != CallState.IDLE,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                ActiveCallOverlay(viewModel = viewModel)
            }
        }
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onAdd = { name, phone, persona ->
                viewModel.addCustomContact(name, phone, persona)
                showAddContactDialog = false
            }
        )
    }
}

@Composable
fun DialerScreen(viewModel: CallViewModel) {
    val dialerInput by viewModel.dialerInput.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Premium Logo/Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_calling_hero),
                    contentDescription = "Calling Header",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.35f
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Unlimited Free P2P VoIP Calls",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Dial any number or talk to professional AI companion operators instantly.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Dialer Display Number
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dialerInput.ifEmpty { "Enter Number" },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dialerInput.isEmpty()) Color.DarkGray else Color.White,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dialer_display")
                )

                if (dialerInput.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.deleteFromDialer() },
                        modifier = Modifier.testTag("dialer_backspace")
                    ) {
                        Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = BrightPink)
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 32.dp),
                color = DeepIndigo,
                thickness = 1.dp
            )
        }

        // Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                listOf('1' to "", '2' to "A B C", '3' to "D E F"),
                listOf('4' to "G H I", '5' to "J K L", '6' to "M N O"),
                listOf('7' to "P Q R S", '8' to "T U V", '9' to "W X Y Z"),
                listOf('*' to "", '0' to "+", '#' to "")
            )

            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        KeypadButton(
                            digit = key.first,
                            letters = key.second,
                            onClick = { viewModel.appendToDialer(key.first) }
                        )
                    }
                }
            }
        }

        // Launch Call Button
        IconButton(
            onClick = { viewModel.initiateCallFromDialer() },
            enabled = dialerInput.isNotEmpty(),
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    if (dialerInput.isNotEmpty()) PhoneGreen else DeepIndigo
                )
                .testTag("dial_call_button")
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Place Call",
                tint = if (dialerInput.isNotEmpty()) SlateBg else Color.Gray,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun KeypadButton(
    digit: Char,
    letters: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(CardBg)
            .clickable(onClick = onClick)
            .border(1.dp, DeepIndigo, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(viewModel: CallViewModel) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phoneNumber.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search contact name or number...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("contact_search_bar"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrightCyan,
                unfocusedBorderColor = DeepIndigo,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = "Empty",
                        tint = DeepIndigo,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No contacts found",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredContacts, key = { it.id }) { contact ->
                    ContactRow(
                        contact = contact,
                        onCall = { viewModel.initiateCall(contact) },
                        onDelete = { viewModel.deleteContact(contact) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactRow(
    contact: ContactEntity,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    val avatarColors = listOf(
        Color(0xFFFA26A0), Color(0xFF00F0FF), Color(0xFFF8A44C),
        Color(0xFF43DF8B), Color(0xFF8B5CF6), Color(0xFFEF4444)
    )
    val colorIndex = remember(contact.avatarColorSeed) {
        val index = contact.avatarColorSeed.mod(avatarColors.size)
        if (index < 0) index + avatarColors.size else index
    }
    val avatarColor = avatarColors[colorIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onCall,
                onLongClick = onDelete
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.25f))
                    .border(1.dp, avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = avatarColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // Pulse indicator for AI helper status
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ActiveGreen)
                        .align(Alignment.BottomEnd)
                        .border(1.5.dp, CardBg, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (contact.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DeepIndigo)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Persona", fontSize = 8.sp, color = BrightCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    text = contact.phoneNumber,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Call Icon Action
            IconButton(
                onClick = onCall,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DeepIndigo)
                    .testTag("call_contact_${contact.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Contact",
                    tint = BrightCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CallHistoryScreen(viewModel: CallViewModel) {
    val logs by viewModel.callLogs.collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Calls",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (logs.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = BrightPink, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", color = BrightPink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Empty History",
                        tint = DeepIndigo,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your call logs will appear here",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    CallLogItem(
                        log = log,
                        onDelete = { viewModel.deleteCallLog(log.id) },
                        onDialBack = {
                            // Find contact or generate temp and call
                            viewModel.setDialerInput(log.phoneNumber)
                            viewModel.initiateCallFromDialer()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CallLogItem(
    log: CallLogEntity,
    onDelete: () -> Unit,
    onDialBack: () -> Unit
) {
    val formattedTime = remember(log.timestamp) {
        val date = Date(log.timestamp)
        val format = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        format.format(date)
    }

    val formattedDuration = remember(log.durationSeconds) {
        val minutes = log.durationSeconds / 60
        val seconds = log.durationSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDialBack),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Log Icon indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DeepIndigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallMade,
                    contentDescription = "Outgoing call",
                    tint = PhoneGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.contactName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.phoneNumber,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = Color.DarkGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            // Duration and Delete Action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedDuration,
                    color = BrightCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp).testTag("delete_log_${log.id}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove log", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun ActiveCallOverlay(viewModel: CallViewModel) {
    val contact by viewModel.activeCallContact.collectAsStateWithLifecycle()
    val callState by viewModel.activeCallState.collectAsStateWithLifecycle()
    val duration by viewModel.activeCallDuration.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsStateWithLifecycle()
    val isHold by viewModel.isCallHold.collectAsStateWithLifecycle()
    val transcript by viewModel.callTranscript.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val userInputText by viewModel.userInputText.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val transcriptListState = rememberLazyListState()

    // Scroll to bottom when transcript updates
    LaunchedEffect(transcript.size) {
        if (transcript.isNotEmpty()) {
            transcriptListState.animateScrollToItem(transcript.size - 1)
        }
    }

    val formattedDuration = remember(duration) {
        val minutes = duration / 60
        val seconds = duration % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    // Call screen background with pulsing circles
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SlateBg, DeepIndigo)
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Callee Avatar and Details
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Glow circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            if (callState == CallState.CONNECTED) BrightCyan.copy(alpha = 0.08f)
                            else BrightPink.copy(alpha = 0.08f)
                        )
                )

                // Main Avatar Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(DeepIndigo)
                        .border(
                            2.dp,
                            if (callState == CallState.CONNECTED) BrightCyan else BrightPink,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact?.name?.take(1)?.uppercase() ?: "?",
                        color = if (callState == CallState.CONNECTED) BrightCyan else BrightPink,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = contact?.name ?: "Unknown Operator",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (callState) {
                    CallState.DIALING -> "Dialing secure connection..."
                    CallState.RINGING -> "Ringing online receiver..."
                    CallState.CONNECTED -> if (isHold) "On Hold" else "Connected via VoIP"
                    CallState.DISCONNECTED -> "Call Ended"
                    else -> "Connecting..."
                },
                color = if (callState == CallState.CONNECTED) PhoneGreen else Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (callState == CallState.CONNECTED) {
                Text(
                    text = formattedDuration,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Real-time Chat Transcript Panel (Shows speech during the call)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateBg.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, DeepIndigo)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header of transcript
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isAiThinking) BrightPink else ActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAiThinking) "Operator typing / speaking..." else "Connection secure & active",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Chat bubble list
                LazyColumn(
                    state = transcriptListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (transcript.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Connected! Speak into the line to chat.",
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(transcript) { turn ->
                            ChatBubble(turn = turn)
                        }
                    }
                }

                // AI thinking loader
                if (isAiThinking) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = BrightCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transcribing audio...", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        // Chat & Speak Inputs (For typing/speaking into call line)
        if (callState == CallState.CONNECTED && !isHold) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quick responses list (for rapid easy calling!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickPhrases = listOf(
                        "Hi! How are you?",
                        "Help me practice Spanish",
                        "Tell me a joke!",
                        "Goodbye!"
                    )
                    quickPhrases.forEach { phrase ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepIndigo)
                                .clickable {
                                    viewModel.setUserInputText(phrase)
                                    viewModel.speakIntoCall()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(phrase, fontSize = 10.sp, color = BrightCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Input box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInputText,
                        onValueChange = { viewModel.setUserInputText(it) },
                        placeholder = { Text("Speak / Say anything...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("call_input_text"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightCyan,
                            unfocusedBorderColor = DeepIndigo,
                            focusedContainerColor = CardBg,
                            unfocusedContainerColor = CardBg,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 2,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { viewModel.speakIntoCall() }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.speakIntoCall() },
                        enabled = userInputText.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (userInputText.isNotEmpty()) BrightCyan else DeepIndigo)
                            .testTag("call_send_speech")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Speak",
                            tint = if (userInputText.isNotEmpty()) SlateBg else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Call Controls Row (Mute, Speaker, Hold, End Call)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            CallControlIcon(
                active = isMuted,
                activeIcon = Icons.Default.MicOff,
                inactiveIcon = Icons.Default.Mic,
                label = "Mute",
                onClick = { viewModel.toggleMute() },
                enabled = callState == CallState.CONNECTED
            )

            // Hold Button
            CallControlIcon(
                active = isHold,
                activeIcon = Icons.Default.PlayArrow,
                inactiveIcon = Icons.Default.Pause,
                label = "Hold",
                onClick = { viewModel.toggleHold() },
                enabled = callState == CallState.CONNECTED
            )

            // Speaker Button
            CallControlIcon(
                active = isSpeakerOn,
                activeIcon = Icons.AutoMirrored.Filled.VolumeUp,
                inactiveIcon = Icons.Default.VolumeMute,
                label = "Speaker",
                onClick = { viewModel.toggleSpeaker() },
                enabled = callState == CallState.CONNECTED
            )

            // End Call Button
            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BrightPink)
                    .testTag("active_end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(turn: CallTurn) {
    val bubbleColor = if (turn.isUser) DeepIndigo else CardBg
    val align = if (turn.isUser) Alignment.End else Alignment.Start
    val textColor = if (turn.isUser) Color.White else Color.LightGray

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (turn.isUser) 12.dp else 0.dp,
                        bottomEnd = if (turn.isUser) 0.dp else 12.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 260.dp)
        ) {
            Column {
                Text(
                    text = turn.speakerName,
                    color = if (turn.isUser) BrightCyan else BrightPink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = turn.text,
                    color = textColor,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun CallControlIcon(
    active: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (active) BrightCyan else DeepIndigo.copy(alpha = 0.5f)
                )
                .border(1.dp, DeepIndigo, CircleShape)
        ) {
            Icon(
                imageVector = if (active) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = if (active) SlateBg else if (enabled) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (enabled) Color.LightGray else Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, persona: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var persona by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create AI Companion Persona", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. My Tutor, Detective, Sherlock") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightCyan,
                        unfocusedBorderColor = DeepIndigo,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_name_field")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Virtual Phone Number") },
                    placeholder = { Text("e.g. 555-0909") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightCyan,
                        unfocusedBorderColor = DeepIndigo,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_phone_field")
                )

                OutlinedTextField(
                    value = persona,
                    onValueChange = { persona = it },
                    label = { Text("AI Character Instructions (Persona)") },
                    placeholder = { Text("Describe how they behave, speak and react when the user calls them.") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightCyan,
                        unfocusedBorderColor = DeepIndigo,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_persona_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                        onAdd(name, phone, persona)
                    }
                },
                enabled = name.isNotEmpty() && phone.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = BrightCyan, contentColor = SlateBg),
                modifier = Modifier.testTag("add_contact_confirm_btn")
            ) {
                Text("Save & Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BrightPink, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CardBg
    )
}
