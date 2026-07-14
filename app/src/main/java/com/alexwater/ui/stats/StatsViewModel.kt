package com.alexwater.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexwater.AlexWaterApp
import com.alexwater.model.DaySummary
import com.alexwater.model.WeekStats
import com.alexwater.model.WaterRecord
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as AlexWaterApp).repository

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    val settings: StateFlow<com.alexwater.model.AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.alexwater.model.AppSettings())

    val weekStats: StateFlow<WeekStats> = combine(weekOffset, settings, repo.records) { offset, s, records ->
        computeWeekStats(offset, s.dailyGoalMl, records)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, WeekStats(emptyList(), 0, 0, 0))

    val weekLabels: StateFlow<List<String>> = weekOffset.map { offset ->
        val start = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusWeeks(offset.toLong())
        (0..6).map { start.plusDays(it.toLong()) }.map { "${it.monthValue}/${it.dayOfMonth}" }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setWeek(offset: Int) { _weekOffset.value = offset }

    private fun computeWeekStats(offset: Int, goalMl: Int, allRecords: List<WaterRecord>): WeekStats {
        val today = LocalDate.now()
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset.toLong())
        val end = start.plusDays(6)

        val records = allRecords.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
        val days = (0..6).map { i ->
            val date = start.plusDays(i.toLong())
            val dayRecords = records.filter { rec -> rec.date == date }
            DaySummary(date, dayRecords.sumOf { rec -> rec.amountMl }, goalMl, dayRecords)
        }
        return WeekStats(
            days = days,
            averageMl = if (days.isNotEmpty()) (days.sumOf { d -> d.totalMl }.toFloat() / days.size).toInt() else 0,
            bestMl = days.maxOfOrNull { d -> d.totalMl } ?: 0,
            goalDaysCount = days.count { d -> d.totalMl >= goalMl },
        )
    }
}
