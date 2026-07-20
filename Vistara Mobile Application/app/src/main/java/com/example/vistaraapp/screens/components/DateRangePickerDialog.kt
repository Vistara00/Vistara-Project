package com.example.vistaraapp.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DateRangePickerDialog(
    brandGreen: Color,
    initialSelection: Pair<LocalDate?, LocalDate?>,
    onConfirm: (start: LocalDate, end: LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var localSelection by remember(initialSelection) { mutableStateOf(initialSelection) }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(36) }
    val daysOfWeek = remember { DayOfWeek.entries.let { days ->
        // Reorder so Sunday is first
        listOf(days[6]) + days.subList(0, 6)
    }}

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val coroutineScope = rememberCoroutineScope()
    val darkGray = MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val visibleMonth = calendarState.firstVisibleMonth.yearMonth
                val configuration = LocalConfiguration.current
                val locale = configuration.locales[0]
                val monthName = visibleMonth.month.getDisplayName(
                    java.time.format.TextStyle.FULL, locale
                )
                Text(
                    text = "$monthName ${visibleMonth.year}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandGreen
                )
                Row {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            calendarState.animateScrollToMonth(
                                calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                            )
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Month", tint = brandGreen)
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            calendarState.animateScrollToMonth(
                                calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                            )
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Month", tint = brandGreen)
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val locale = LocalConfiguration.current.locales[0]
                    for (dayOfWeek in daysOfWeek) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = dayOfWeek.getDisplayName(
                                java.time.format.TextStyle.SHORT, locale
                            ).take(3),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = darkGray
                        )
                    }
                }
                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        val isStart = day.date == localSelection.first
                        val isEnd = day.date == localSelection.second
                        val isInRange = isDateInRange(day.date, localSelection)

                        val dayBg = when {
                            isStart || isEnd -> brandGreen
                            isInRange -> brandGreen.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }
                        val dayTextColor = when {
                            isStart || isEnd -> Color.White
                            day.position != DayPosition.MonthDate -> Color.LightGray
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(dayBg, shape = RoundedCornerShape(8.dp))
                                .clickable(
                                    enabled = true,
                                    onClick = {
                                        val clicked = day.date
                                        val start = localSelection.first
                                        val end = localSelection.second
                                        localSelection = when {
                                            start == null -> Pair(clicked, null)
                                            end == null -> if (clicked.isBefore(start)) Pair(clicked, null) else Pair(start, clicked)
                                            else -> Pair(clicked, null)
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                color = dayTextColor,
                                fontSize = 14.sp,
                                fontWeight = if (isStart || isEnd) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val start = localSelection.first
                    val end = localSelection.second
                    if (start != null && end != null) onConfirm(start, end)
                },
                enabled = localSelection.first != null && localSelection.second != null
            ) { Text("OK", color = brandGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = brandGreen) }
        }
    )
}

private fun isDateInRange(date: LocalDate, selection: Pair<LocalDate?, LocalDate?>): Boolean {
    val start = selection.first
    val end = selection.second
    if (start != null && end != null) {
        return !date.isBefore(start) && !date.isAfter(end)
    }
    return date == start || date == end
}
