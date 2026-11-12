package br.gov.sp.cps.projetofinal_aplicativoandroid.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val preferencesRepository = UserPreferencesRepository(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = preferencesRepository.userPreferencesFlow.first()
                if (prefs.isReminderEnabled) {
                    NotificationHelper.showReminderNotification(
                        context = context,
                        streak = prefs.streakCount
                    )
                }
            } catch (e: Exception) {
                NotificationHelper.showReminderNotification(context = context, streak = 1)
            }
        }
    }
}
