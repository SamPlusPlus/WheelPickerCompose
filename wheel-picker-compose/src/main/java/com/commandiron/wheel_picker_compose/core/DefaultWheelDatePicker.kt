package com.commandiron.wheel_picker_compose.core

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun DefaultWheelDatePicker(
    modifier: Modifier = Modifier,
    startDate: LocalDate = LocalDate.now(),
    yearsRange: IntRange? = IntRange(1922, 2122),
    backwardsDisabled: Boolean = false,
    size: DpSize = DpSize(256.dp, 128.dp),
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = LocalContentColor.current,
    selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
    onSnappedDate : (snappedDate: SnappedDate) -> Int? = { _ -> null }
) {
    var dayTexts by remember { mutableStateOf((1..31).toList().map { it.toString() }) }
    val monthTexts: List<String> = if(size.width / 3 < 55.dp){
        DateFormatSymbols().shortMonths.toList()
    }else{
        DateFormatSymbols().months.toList()
    }

    val yearTexts = yearsRange?.map { it.toString() } ?: listOf()

    var snappedDate by remember { mutableStateOf(startDate) }

    Box(modifier = modifier, contentAlignment = Alignment.Center){
        if(selectorProperties.enabled().value){
            Surface(
                modifier = Modifier
                    .size(size.width, size.height / 3),
                shape = selectorProperties.shape().value,
                color = selectorProperties.color().value,
                border = selectorProperties.border().value
            ) {}
        }
        Row {
            //Day of Month
            WheelTextPicker(
                size = DpSize(size.width / 3 / if(yearsRange == null) 2 else 1, size.height),
                texts = dayTexts,
                style = textStyle,
                color = textColor,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    enabled = false
                ),
                startIndex = startDate.dayOfMonth - 1,
                onScrollFinished = { snappedIndex ->

                    val newDate = snappedDate.withDayOfMonth(if(snappedIndex + 1 > dayTexts.size) snappedIndex else snappedIndex + 1)
                    val isDateBefore = isDateBefore(newDate, startDate)

                    if(backwardsDisabled) {
                        if(!isDateBefore) {
                            snappedDate = newDate
                        }
                    } else {
                        snappedDate = newDate
                    }

                    onSnappedDate(
                        SnappedDate.DayOfMonth(snappedDate, snappedDate.dayOfMonth - 1)
                    )?.let { return@WheelTextPicker it }

                    return@WheelTextPicker snappedDate.dayOfMonth - 1
                }
            )
            //Month
            WheelTextPicker(
                size = DpSize(size.width / 3, size.height),
                texts = monthTexts,
                style = textStyle,
                color = textColor,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    enabled = false
                ),
                startIndex = startDate.month.value - 1,
                onScrollFinished = { snappedIndex ->

                    val newDate = snappedDate.withMonth(if(snappedIndex + 1 > 12) 12 else snappedIndex + 1)
                    val isDateBefore = isDateBefore(newDate, startDate)

                    if(backwardsDisabled) {
                        if(!isDateBefore) {
                            snappedDate = newDate
                        }
                    } else {
                        snappedDate = newDate
                    }

                    dayTexts = calculateMonthDayTexts(snappedDate.month.value, snappedDate.year)

                    onSnappedDate(
                        SnappedDate.Month(snappedDate, snappedDate.month.value - 1)
                    )?.let { return@WheelTextPicker it }

                    return@WheelTextPicker snappedDate.month.value - 1
                }
            )
            //Year
            yearsRange?.let { yearsRange ->
                WheelTextPicker(
                    size = DpSize(size.width / 3, size.height),
                    texts = yearTexts,
                    style = textStyle,
                    color = textColor,
                    selectorProperties = WheelPickerDefaults.selectorProperties(
                        enabled = false
                    ),
                    startIndex = if(yearsRange.indexOf(yearsRange.find { it == startDate.year }) == -1) {
                        throw IllegalArgumentException(
                            "startDate.year should greater than minYear and smaller than maxYear"
                        )
                    } else yearsRange.indexOf(yearsRange.find { it == startDate.year }),
                    onScrollFinished = { snappedIndex ->

                        val selectedYearText = yearTexts.getOrNull(snappedIndex) ?: yearTexts.last()
                        val newDate = snappedDate.withYear(selectedYearText.toInt())
                        val isDateBefore = isDateBefore(newDate, startDate)

                        if(backwardsDisabled) {
                            if(!isDateBefore) {
                                snappedDate = newDate
                            }
                        } else {
                            snappedDate = newDate
                        }

                        dayTexts = calculateMonthDayTexts(snappedDate.month.value, snappedDate.year)

                        onSnappedDate(SnappedDate.Year(snappedDate, yearTexts.indexOf(snappedDate.year.toString())))?.let { return@WheelTextPicker it }

                        return@WheelTextPicker yearTexts.indexOf(snappedDate.year.toString())
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun isDateBefore(date: LocalDate, currentDate: LocalDate): Boolean{
    return date.isBefore(currentDate)
}