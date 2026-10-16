package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.Box
import com.example.ui.theme.AvatarPalette
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.PhoneNumberFormatter
import com.example.domain.util.SmartTextParser
import com.example.domain.util.SmsEncodingUtils
import com.example.ui.components.SmsGramTopBar
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings
import com.example.ui.theme.smsGramColors
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    viewModel: SmsViewModel,
    initialRecipient: String? = null,
    initialBody: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = appStrings()
    val currentLanguage = LocalAppLanguage.current
    val scrollState = rememberScrollState()
    var recipient by remember(initialRecipient) { mutableStateOf(initialRecipient ?: "") }
    var messageBody by remember(initialBody) { mutableStateOf(initialBody ?: "") }
    val colors = smsGramColors

    val activeSims by viewModel.activeSims.collectAsState()
    val selectedSimIndex by viewModel.selectedSimIndex.collectAsState()
    val deviceContacts by viewModel.deviceContacts.collectAsState()
    var showSimModal by remember { mutableStateOf(false) }

    var hasContactsPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPerm = isGranted
        if (isGranted) {
            viewModel.loadDeviceContacts()
        }
    }

    LaunchedEffect(hasContactsPerm) {
        if (hasContactsPerm) {
            viewModel.loadDeviceContacts()
        }
    }

    // System Contact Picker Launcher with multi-fallback URI resolution
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            if (contactUri != null) {
                val resolvedNumber = resolvePhoneFromContactUri(context, contactUri)
                if (!resolvedNumber.isNullOrBlank()) {
                    recipient = resolvedNumber
                }
            }
        }
    }

    fun openSystemContactPicker() {
        if (!hasContactsPerm) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            return
        }
        try {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                contactPickerLauncher.launch(fallbackIntent)
            } catch (ex: Exception) {
                try {
                    val genericIntent = Intent(Intent.ACTION_PICK).apply {
                        type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    }
                    contactPickerLauncher.launch(genericIntent)
                } catch (e3: Exception) {
                    e3.printStackTrace()
                }
            }
        }
    }

    val countInfo = remember(messageBody) {
        SmsEncodingUtils.calculateSmsCount(messageBody)
    }
    val encodingLabel = if (countInfo.encoding == SmsEncodingUtils.EncodingType.UNICODE) {
        strings.persianEncoding
    } else "GSM"

    val currentSim = activeSims.getOrNull(selectedSimIndex) ?: activeSims.firstOrNull()
    val simBrandColor = currentSim?.getThemeHarmonizedColor() ?: MaterialTheme.colorScheme.primary

    // Filter contacts based on current recipient query
    val filteredContacts = remember(recipient, deviceContacts) {
        val q = recipient.trim()
        if (q.isBlank()) {
            deviceContacts.take(40)
        } else {
            val normQ = SmartTextParser.normalizeDigits(q).replace(" ", "").replace("-", "")
            deviceContacts.filter { c ->
                val normNum = SmartTextParser.normalizeDigits(c.number).replace(" ", "").replace("-", "")
                c.name.contains(q, ignoreCase = true) ||
                normNum.contains(normQ) ||
                (q.length >= 4 && normNum.contains(q))
            }.take(30)
        }
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            val canSend = recipient.isNotBlank() && messageBody.isNotBlank()
            SmsGramTopBar(
                title = strings.newMessage,
                onBack = onBack,
                actions = {
                    Text(
                        text = strings.send,
                        color = if (canSend) MaterialTheme.colorScheme.primary else colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = canSend) {
                                val sim = activeSims.getOrNull(selectedSimIndex)
                                val subId = sim?.subscriptionId ?: -1
                                viewModel.sendMessage(
                                    recipient = recipient.trim(),
                                    body = messageBody.trim(),
                                    subId = subId
                                )
                                viewModel.navigateTo(ScreenDestination.Home)
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Inset Card for Recipient + Clear & Contact Picker Buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.toLabel,
                                color = colors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = recipient,
                                onValueChange = { recipient = it },
                                placeholder = {
                                    Text(
                                        text = strings.newRecipientHint,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            if (recipient.isNotBlank()) {
                                IconButton(
                                    onClick = { recipient = "" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = strings.clearSearch,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Contact Picker Button
                            IconButton(
                                onClick = { openSystemContactPicker() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = strings.chooseFromContacts,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (activeSims.size > 1) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.sendWithSim,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.weight(1f))

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(simBrandColor.copy(alpha = 0.12f))
                                        .clickable { showSimModal = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(simBrandColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${(currentSim?.slotIndex ?: 0) + 1}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    val defaultSimName = strings.sim1
                                    Text(
                                        text = currentSim?.displayTitle ?: defaultSimName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = simBrandColor
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = strings.selectSimForSending,
                                        tint = simBrandColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Message Body Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = messageBody,
                            onValueChange = { messageBody = it },
                            placeholder = {
                                Text(
                                    text = strings.typeMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val counterText = if (currentLanguage == AppLanguage.PERSIAN) {
                                JalaliCalendar.toPersianDigits("${countInfo.remainingInSegment}/${countInfo.segmentCount} پیامک ($encodingLabel)")
                            } else {
                                "${countInfo.remainingInSegment}/${countInfo.segmentCount} SMS ($encodingLabel)"
                            }
                            Text(
                                text = counterText,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )

                            if (recipient.isNotBlank() && messageBody.isBlank()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable {
                                            viewModel.openConversationForRecipient(recipient.trim())
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.PERSIAN) "ورود مستقیم به گفتگو" else "Open Chat",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contacts Permission Card (shown if permission is not granted)
                if (!hasContactsPerm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.contactsPermissionRequired,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.grantContactsPermission,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = strings.grantPermission,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Device Contacts Suggestions / Fast Contact Picker Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openSystemContactPicker() }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.openPhoneContacts,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = strings.chooseFromContacts,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        if (filteredContacts.isNotEmpty()) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.deviceContacts,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = if (currentLanguage == AppLanguage.PERSIAN) {
                                        JalaliCalendar.toPersianDigits("${filteredContacts.size} مخاطب")
                                    } else {
                                        "${filteredContacts.size} contacts"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            filteredContacts.forEachIndexed { idx, contact ->
                                val gradient = AvatarPalette.getGradientFor(contact.name.ifBlank { contact.number }, isDarkTheme)
                                val initials = contact.name.trim().take(2).uppercase().ifBlank { "#" }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            recipient = contact.number.trim()
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(gradient)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = contact.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = PhoneNumberFormatter.format(contact.number, currentLanguage == AppLanguage.PERSIAN),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (idx < filteredContacts.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        modifier = Modifier.padding(start = 48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSimModal) {
        com.example.ui.components.SimSelectionModal(
            activeSims = activeSims,
            selectedSimIndex = selectedSimIndex,
            onSimSelected = { index, _ ->
                viewModel.setSelectedSimIndex(index)
            },
            onDismiss = { showSimModal = false }
        )
    }
}

private fun resolvePhoneFromContactUri(context: Context, contactUri: Uri): String? {
    var phoneNumber: String? = null

    // Method 1: Query CommonDataKinds.Phone
    try {
        context.contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numIdx != -1) {
                    val n = cursor.getString(numIdx)
                    if (!n.isNullOrBlank()) phoneNumber = n.trim()
                }
            }
        }
    } catch (e: Exception) {
        // Fallback to Method 2
    }

    // Method 2: Query ContactsContract.Contacts._ID and look up Phone table
    if (phoneNumber.isNullOrBlank()) {
        try {
            var contactId: String? = null
            context.contentResolver.query(
                contactUri,
                arrayOf(ContactsContract.Contacts._ID),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIdx != -1) contactId = cursor.getString(idIdx)
                }
            }

            if (!contactId.isNullOrBlank()) {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numIdx != -1) {
                            val n = cursor.getString(numIdx)
                            if (!n.isNullOrBlank()) phoneNumber = n.trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to Method 3
        }
    }

    // Method 3: Universal vendor column search (MIUI, Samsung, ColorOS, etc.)
    if (phoneNumber.isNullOrBlank()) {
        try {
            context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val possibleCols = listOf("data1", "number", "phone_number", "data4", "normalized_number")
                    for (col in possibleCols) {
                        val idx = cursor.getColumnIndex(col)
                        if (idx != -1) {
                            val v = cursor.getString(idx)
                            if (!v.isNullOrBlank() && v.any { it.isDigit() }) {
                                phoneNumber = v.trim()
                                break
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return phoneNumber
}
