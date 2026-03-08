package com.xingheyuzhuan.shiguangschedule.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.xingheyuzhuan.shiguangschedule.R
import com.xingheyuzhuan.shiguangschedule.ui.schedule.MergedCourseBlock

/**
 * 渲染单个课程块的 UI 组件。
 * 它负责展示课程信息、颜色，并处理冲突标记。
 */
@Composable
fun CourseBlock(
    mergedBlock: MergedCourseBlock,
    style: ScheduleGridStyleComposed,
    modifier: Modifier = Modifier,
    isCurrentWeek: Boolean = true,
    startTime: String? = null
) {
    val firstCourse = mergedBlock.courses.firstOrNull()
    val isDarkTheme = isSystemInDarkTheme() // 获取当前主题模式

    val conflictColorAdapted = if (isDarkTheme) {
        style.conflictCourseColorDark // 使用深色冲突色
    } else {
        style.conflictCourseColor // 使用浅色冲突色
    }

    // 尝试获取颜色索引 (colorInt)
    val colorIndex = firstCourse?.course?.colorInt
        // 检查索引是否在映射表范围内，否则返回 null
        ?.takeIf { it in style.courseColorMaps.indices }

    // 适配后的课程颜色，如果 colorIndex 存在
    val courseColorAdapted: Color? = colorIndex?.let { index ->
        val baseColorMap = style.courseColorMaps[index]
        if (isDarkTheme) {
            baseColorMap.dark
        } else {
            baseColorMap.light
        }
    }

    val fallbackColorAdapted: Color = if (isDarkTheme) {
        style.courseColorMaps.first().dark
    } else {
        style.courseColorMaps.first().light
    }

    val shouldDimNonCurrentWeek = style.dimNonCurrentWeekCourses && !isCurrentWeek
    val centeredTextModifier = if (style.centerCourseContent) Modifier.fillMaxWidth() else Modifier
    val textAlign = if (style.centerCourseContent) TextAlign.Center else TextAlign.Start

    val blockColor = if (mergedBlock.isConflict) {
        conflictColorAdapted.copy(alpha = style.courseBlockAlpha)
    } else {
        (courseColorAdapted ?: fallbackColorAdapted).copy(alpha = style.courseBlockAlpha)
    }.let { base ->
        if (shouldDimNonCurrentWeek) base.copy(alpha = base.alpha * 0.5f) else base
    }

    val textColor = if (shouldDimNonCurrentWeek) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    // --- 字体大小计算逻辑 (新增) ---
    // 通过将基准字号乘以缩放因子，实现全局联动
    val s13 = (13 * style.fontScale).sp
    val s12 = (12 * style.fontScale).sp
    val s10 = (10 * style.fontScale).sp

    val customStartTime = firstCourse?.course?.customStartTime
    val customEndTime = firstCourse?.course?.customEndTime
    val customTimeString = if (customStartTime != null && customEndTime != null) {
        "$customStartTime - $customEndTime"
    } else {
        null
    }
    val isCustomTimeCourse = customTimeString != null

    Box(
        modifier = modifier
            .padding(style.courseBlockOuterPadding)
            .clip(RoundedCornerShape(style.courseBlockCornerRadius))
            .background(color = blockColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(style.courseBlockInnerPadding),
            horizontalAlignment = if (style.centerCourseContent) Alignment.CenterHorizontally else Alignment.Start
        ) {
            if (mergedBlock.isConflict) {
                // 冲突状态下的字体缩放
                mergedBlock.courses.forEach { course ->
                    Text(
                        text = course.course.name,
                        fontSize = s12, // 使用缩放后的 12sp
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        overflow = TextOverflow.Ellipsis,
                        modifier = centeredTextModifier.weight(1f, fill = false),
                        textAlign = textAlign
                    )
                }
                Text(
                    text = stringResource(R.string.label_conflict),
                    fontSize = s10, // 使用缩放后的 10sp
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    modifier = centeredTextModifier.padding(top = 2.dp),
                    textAlign = textAlign
                )
            } else {
                // --- 1. 时间显示层 ---
                if (isCustomTimeCourse) {
                    Text(
                        text = customTimeString,
                        fontSize = s10, // 使用缩放后的 10sp
                        color = textColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(lineHeight = 1.em),
                        modifier = centeredTextModifier,
                        textAlign = textAlign
                    )
                } else if (style.showStartTime && startTime != null) {
                    Text(
                        text = startTime,
                        fontSize = s10, // 使用缩放后的 10sp
                        color = textColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(lineHeight = 1.em),
                        modifier = centeredTextModifier,
                        textAlign = textAlign
                    )
                }

                // --- 2. 课程名称 ---
                Text(
                    text = firstCourse?.course?.name ?: "",
                    fontSize = s13,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    overflow = TextOverflow.Ellipsis,
                    modifier = centeredTextModifier.weight(1f, fill = false),
                    style = TextStyle(lineHeight = 1.2.em),
                    textAlign = textAlign
                )

                // --- 3. 教师 (受 hideTeacher 开关控制) ---
                if (!style.hideTeacher) { // 如果不隐藏，则显示
                    val teacher = firstCourse?.course?.teacher ?: ""
                    if (teacher.isNotBlank()) {
                        Text(
                            text = teacher,
                            fontSize = s10,
                            color = textColor,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(lineHeight = 1.em),
                            modifier = centeredTextModifier,
                            textAlign = textAlign
                        )
                    }
                }

                // --- 4. 地点 (受 hideLocation 和 removeLocationAt 开关控制) ---
                if (!style.hideLocation) { // 如果不隐藏，则显示
                    val position = firstCourse?.course?.position ?: ""
                    if (position.isNotBlank()) {
                        // 根据 removeLocationAt 决定前缀
                        val prefix = if (style.removeLocationAt) "" else "@"
                        Text(
                            text = "$prefix$position",
                            fontSize = s10,
                            color = textColor,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(lineHeight = 1.2.em),
                            modifier = centeredTextModifier,
                            textAlign = textAlign
                        )
                    }
                }

                // --- 5. 非本周课程提示 (受 dimNonCurrentWeekCourses 开关控制) ---
                if (shouldDimNonCurrentWeek) {
                    Text(
                        text = "（非本周）",
                        fontSize = s10,
                        color = textColor,
                        style = TextStyle(lineHeight = 1.2.em),
                        modifier = centeredTextModifier.padding(top = 2.dp),
                        textAlign = textAlign
                    )
                }
            }
        }
    }
}