package com.alexwater.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexwater.AlexWaterApp
import com.alexwater.model.AppSettings
import com.alexwater.model.Theme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as AlexWaterApp).repository

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun updateGoal(ml: Int) {
        viewModelScope.launch {
            repo.updateDailyGoal(ml)
        }
    }

    fun updateCupSize(index: Int, ml: Int) {
        val sizes = settings.value.cupSizes.toMutableList()
        if (index in sizes.indices) {
            sizes[index] = ml
            viewModelScope.launch {
                repo.updateCupSizes(sizes)
            }
        }
    }

    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            repo.updateTheme(theme)
        }
    }
}
