package com.xingheyuzhuan.shiguangschedule.ui.schedule.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xingheyuzhuan.shiguangschedule.R
import com.xingheyuzhuan.shiguangschedule.data.db.main.CourseWithWeeks
import com.xingheyuzhuan.shiguangschedule.data.db.main.TimeSlot

/**
 * 课程选择底部动作条（用于同格多课程选择）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictCourseBottomSheet(
    courses: List<CourseWithWeeks>,
    timeSlots: List<TimeSlot>,
    style: ScheduleGridStyleComposed,
    onCourseClicked: (CourseWithWeeks) -> Unit,
    onDismissRequest: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val fallbackColorAdapted = if (isDarkTheme) {
        style.courseColorMaps.first().dark
    } else {
        style.courseColorMaps.first().light
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(courses) { courseWithWeeks ->
                    val course = courseWithWeeks.course

                    val isCustomTimeCourse = course.customStartTime != null && course.customEndTime != null

                    val startSlot = timeSlots.find { it.number == course.startSection }?.startTime ?: "N/A"
                    val endSlot = timeSlots.find { it.number == course.endSection }?.endTime ?: "N/A"
                    val weeksText = stringResource(
                        R.string.label_weeks_format,
                        formatWeekRanges(courseWithWeeks.weeks.map { it.weekNumber })
                            .ifBlank { stringResource(R.string.label_none) }
                    )

                    val colorIndex = course.colorInt.takeIf { it in style.courseColorMaps.indices }
                    val cardBaseColor = colorIndex?.let { index ->
                        val dualColor = style.courseColorMaps[index]
                        if (isDarkTheme) dualColor.dark else dualColor.light
                    } ?: fallbackColorAdapted

                    val cardColor = cardBaseColor.copy(alpha = style.courseBlockAlpha)
                    val textColor = MaterialTheme.colorScheme.onSurface

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCourseClicked(courseWithWeeks) },
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = textColor
                            )

                            Spacer(Modifier.height(6.dp))

                            if (isCustomTimeCourse) {
                                Text(
                                    text = "${course.customStartTime} - ${course.customEndTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = stringResource(
                                        R.string.course_time_description,
                                        course.startSection!!,
                                        course.endSection!!,
                                        startSlot,
                                        endSlot
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                            }

                            Text(
                                text = weeksText,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = stringResource(R.string.course_position_prefix, course.position),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                            Text(
                                text = stringResource(R.string.course_teacher_prefix, course.teacher),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatWeekRanges(weeks: List<Int>): String {
    if (weeks.isEmpty()) return ""

    val sorted = weeks.distinct().sorted()
    val ranges = mutableListOf<String>()
    var start = sorted.first()
    var end = start

    for (i in 1 until sorted.size) {
        val value = sorted[i]
        if (value == end + 1) {
            end = value
        } else {
            ranges += if (start == end) "$start" else "$start-$end"
            start = value
            end = value
        }
    }
    ranges += if (start == end) "$start" else "$start-$end"

    return ranges.joinToString(",")
}

