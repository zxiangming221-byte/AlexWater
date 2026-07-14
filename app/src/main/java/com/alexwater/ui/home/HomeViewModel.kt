package com.alexwater.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexwater.AlexWaterApp
import com.alexwater.model.WaterRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as AlexWaterApp).repository

    private val _todayMl = MutableStateFlow(0)
    val todayMl: StateFlow<Int> = _todayMl.asStateFlow()

    private val _todayRecords = MutableStateFlow<List<WaterRecord>>(emptyList())
    val todayRecords: StateFlow<List<WaterRecord>> = _todayRecords.asStateFlow()

    val settings = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.alexwater.model.AppSettings())

    val progress: StateFlow<Float> = combine(_todayMl, settings) { ml, s ->
        (ml.toFloat() / s.dailyGoalMl).coerceIn(0f, 1f)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    val isComplete: StateFlow<Boolean> = progress.map { it >= 1f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Observe today's records from Room
        viewModelScope.launch {
            repository.getRecordsByDate(LocalDate.now()).collect { records ->
                _todayRecords.value = records.sortedByDescending { it.timestamp }
                _todayMl.value = records.sumOf { it.amountMl }
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch { repository.addRecord(amountMl) }
    }

    fun deleteRecord(record: WaterRecord) {
        viewModelScope.launch { repository.deleteRecord(record) }
    }
}
