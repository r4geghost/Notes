package com.dyusov.notes.presentation.utils

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

    fun formatDateToString(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < millisInHour -> "Just now" // если прошло меньше часа, то пишет "Только что"
            diff < millisInDay -> {
                val hours = TimeUnit.MICROSECONDS.toHours(diff)
                "$hours h ago" // если прошло меньше дня, то пишет "n часов назад"
            }
            else -> formater.format(timestamp) // иначе отображаем полную дату
        }
    }
}