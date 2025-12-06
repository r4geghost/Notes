package com.dyusov.notes.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dyusov.notes.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object DateFormatter {

    private val millisInHour = TimeUnit.HOURS.toMillis(1)
    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val formater = SimpleDateFormat.getDateInstance(DateFormat.SHORT)

    fun formatCurrentDate(): String {
        return formater.format(System.currentTimeMillis())
    }

    @Composable
    fun formatDateToString(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < millisInHour -> stringResource(R.string.just_now) // если прошло меньше часа, то пишет "Только что"
            diff < millisInDay -> {
                val hours = TimeUnit.MICROSECONDS.toHours(diff)
                stringResource(R.string.h_ago, hours) // если прошло меньше дня, то пишет "n часов назад"
            }
            else -> formater.format(timestamp) // иначе отображаем полную дату
        }
    }
}