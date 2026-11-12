package br.gov.sp.cps.projetofinal_aplicativoandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import br.gov.sp.cps.projetofinal_aplicativoandroid.notification.NotificationHelper
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.navigation.AppNavigation
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.theme.ProjetoFinalAplicativoAndroidTheme
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel on launch
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val userPreferences by viewModel.userPreferences.collectAsState()

            // Dark theme: explicit preference or system default
            val useDarkTheme = userPreferences.isDarkMode || isSystemInDarkTheme()

            ProjetoFinalAplicativoAndroidTheme(darkTheme = useDarkTheme) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
