package com.alexwater.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexwater.AlexWaterApp
import com.alexwater.model.DaySummary
import com.alexwater.model.MonthStats
import com.alexwater.model.WaterRecord
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as AlexWaterApp).repository

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    val settings: StateFlow<com.alexwater.model.AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.alexwater.model.AppSettings())

    val monthStats: StateFlow<MonthStats> = combine(currentMonth, settings, repo.records) { month, s, records ->
        computeMonthStats(month, s.dailyGoalMl, records)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MonthStats(emptyList(), 0, 0, 0f))

    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    fun nextMonth() {
        val now = YearMonth.now()
        if (_currentMonth.value.isBefore(now)) _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    private fun computeMonthStats(month: YearMonth, goalMl: Int, allRecords: List<WaterRecord>): MonthStats {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        val records = allRecords.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }

        val days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val dayRecords = records.filter { rec -> rec.date == date }
            DaySummary(date, dayRecords.sumOf { rec -> rec.amountMl }, goalMl, dayRecords)
        }
        val totalMl = days.sumOf { d -> d.totalMl }
        return MonthStats(
            days = days,
            averageMl = if (days.isNotEmpty()) (totalMl.toFloat() / days.size).toInt() else 0,
            goalDaysCount = days.count { d -> d.totalMl >= goalMl },
            totalLiters = totalMl / 1000f,
        )
    }
}
