package com.example.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarSystem
import com.example.domain.scheduling.QuickScheduleOption
import com.example.domain.scheduling.ScheduleValidationError
import com.example.domain.scheduling.ScheduleValidator
import com.example.domain.scheduling.ValidationResult
import com.example.domain.scheduling.quickOptionEpoch
import com.example.domain.util.AppDateFormatter
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.ShamsiDateUtils
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings
import com.example.ui.theme.smsGramColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Professional Schedule Sheet matching S4 requirements.
 * Offers quick chips, custom Material 3 DatePicker & TimePicker,
 * Persian/English date preview, inline validation, and schedule confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSheet(
    recipient: String,
    messageBody: String,
    initialEpoch: Long? = null,
    calendarSystem: CalendarSystem = CalendarSystem.SHAMSI,
    usePersianDigits: Boolean = true,
    onDismissRequest: () -> Unit,
    onScheduleConfirmed: (scheduledEpoch: Long) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val strings = appStrings()
    val colors = smsGramColors
    val currentLanguage = LocalAppLanguage.current
    val is24Hour = remember { DateFormat.is24HourFormat(context) }

    val now = remember { System.currentTimeMillis() }
    var selectedOption by remember {
        mutableStateOf(if (initialEpoch != null) QuickScheduleOption.CUSTOM else QuickScheduleOption.HOUR_1)
    }
    var targetEpoch by remember {
        mutableLongStateOf(initialEpoch ?: quickOptionEpoch(QuickScheduleOption.HOUR_1, now))
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val validationResult by remember(recipient, messageBody, targetEpoch) {
        derivedStateOf {
            ScheduleValidator.validate(recipient, messageBody, targetEpoch)
        }
    }

    val isValid = validationResult is ValidationResult.Ok

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Title
            Text(
                text = strings.scheduleSheetTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Message Preview (first 2 lines)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = messageBody.ifBlank { strings.messageBody },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick options chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf(
                    QuickScheduleOption.MIN_10 to strings.scheduleQuick10Min,
                    QuickScheduleOption.HOUR_1 to strings.scheduleQuick1Hour,
                    QuickScheduleOption.TONIGHT_20 to strings.scheduleQuickTonight20,
                    QuickScheduleOption.TOMORROW_09 to strings.scheduleQuickTomorrow09,
                    QuickScheduleOption.CUSTOM to strings.scheduleQuickCustom
                )

                options.forEach { (opt, label) ->
                    val isSelected = (selectedOption == opt)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedOption = opt
                            if (opt == QuickScheduleOption.CUSTOM) {
                                showDatePickerDialog = true
                            } else {
                                targetEpoch = quickOptionEpoch(opt, System.currentTimeMillis())
                            }
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preview line: "Sends: <date> at <time>"
            val previewText = remember(targetEpoch, currentLanguage, calendarSystem, usePersianDigits) {
                val dateStr = AppDateFormatter.formatDate(
                    epochMs = targetEpoch,
                    system = calendarSystem,
                    persianDigits = usePersianDigits,
                    isPersianLanguage = (currentLanguage == com.example.data.model.AppLanguage.PERSIAN)
                )
                val timeStr = AppDateFormatter.formatMessageTime(targetEpoch, usePersianDigits)
                String.format(strings.scheduleSendsAt, dateStr, timeStr)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .clickable { showDatePickerDialog = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.editTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Inline Validation error message if invalid
            if (!isValid) {
                Spacer(modifier = Modifier.height(8.dp))
                val errorMsg = when ((validationResult as? ValidationResult.Error)?.error) {
                    ScheduleValidationError.RECIPIENT -> strings.scheduleErrorRecipient
                    ScheduleValidationError.BODY -> strings.scheduleErrorBody
                    ScheduleValidationError.TOO_SOON -> strings.scheduleErrorTooSoon
                    ScheduleValidationError.TOO_FAR -> strings.scheduleErrorTooFar
                    null -> ""
                }
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.danger,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.cancel)
                }

                Button(
                    onClick = {
                        if (isValid) {
                            onScheduleConfirmed(targetEpoch)
                        }
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.scheduleButtonConfirm)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetEpoch
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            // Retain current hour and minute from targetEpoch
                            val originalCal = Calendar.getInstance().apply { timeInMillis = targetEpoch }
                            val pickedCalUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selectedMillis
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, pickedCalUtc.get(Calendar.YEAR))
                                set(Calendar.MONTH, pickedCalUtc.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, pickedCalUtc.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, originalCal.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, originalCal.get(Calendar.MINUTE))
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            targetEpoch = localCal.timeInMillis
                        }
                        showDatePickerDialog = false
                        showTimePickerDialog = true
                    }
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material 3 Time Picker Dialog
    if (showTimePickerDialog) {
        val initialCal = remember(targetEpoch) {
            Calendar.getInstance().apply { timeInMillis = targetEpoch }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialCal.get(Calendar.MINUTE),
            is24Hour = is24Hour
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val localCal = Calendar.getInstance().apply {
                            timeInMillis = targetEpoch
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        targetEpoch = localCal.timeInMillis
                        showTimePickerDialog = false
                    }
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text(strings.cancel)
                }
            },
            title = { Text(strings.scheduleSheetTitle) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
