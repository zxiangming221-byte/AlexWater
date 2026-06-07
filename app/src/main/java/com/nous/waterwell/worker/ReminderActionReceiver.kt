package com.nous.waterwell.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nous.waterwell.data.database.AppDatabase
import com.nous.waterwell.data.model.DrinkRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles notification action button clicks (Drink 250ml, Drink 500ml, Skip).
 * Runs a coroutine to log the drink record to Room.
 */
class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationHelper.ACTION_DRINK_250 -> {
                logDrink(context, 250)
            }
            NotificationHelper.ACTION_DRINK_500 -> {
                logDrink(context, 500)
            }
            NotificationHelper.ACTION_SKIP -> {
                // Just dismiss — notification is autoCancel=true
            }
        }
    }

    private fun logDrink(context: Context, amountMl: Int) {
        val dao = AppDatabase.getInstance(context).drinkDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(DrinkRecord(amountMl = amountMl, note = "提醒后喝水"))
        }
    }
}
