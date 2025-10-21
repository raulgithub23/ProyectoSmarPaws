package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.*

class AppointmentViewModel : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _selectedTime = MutableStateFlow<String?>(null)
    val selectedTime: StateFlow<String?> = _selectedTime

    private val _currentMonth = MutableStateFlow<YearMonth>(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toYearMonth())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedTime.value = null // reset hora al cambiar fecha
    }

    fun selectTime(time: String) {
        _selectedTime.value = time
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun getAvailableTimesForDate(date: LocalDate): List<String> {
        val baseHours = listOf(
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
            "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30", "18:00"
        )

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        return if (date == today) {
            val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
            baseHours.filter { it.split(":")[0].toInt() > currentHour }.shuffled().take(9)
        } else {
            baseHours.shuffled().take(12)
        }
    }

}

// Extension para YearMonth con kotlinx.datetime
fun LocalDate.toYearMonth(): YearMonth = YearMonth(year, monthNumber)

data class YearMonth(val year: Int, val month: Int) {
    fun lengthOfMonth(): Int = LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
    fun atDay(day: Int) = LocalDate(year, month, day)
    fun plusMonths(n: Int) = YearMonth(year + (month + n - 1) / 12, (month + n - 1) % 12 + 1)
    fun minusMonths(n: Int) = plusMonths(-n)
}
