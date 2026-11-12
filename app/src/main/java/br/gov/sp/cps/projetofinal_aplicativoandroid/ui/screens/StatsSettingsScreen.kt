package br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.preferences.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSettingsScreen(
    userPreferences: UserPreferences,
    totalCards: Int,
    totalReviews: Int,
    totalSuccess: Int,
    onToggleDarkMode: (Boolean) -> Unit,
    onSetDailyGoal: (Int) -> Unit,
    onUpdateReminderSettings: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onTestNotification: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Permission launcher for Android 13+ POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onTestNotification()
        }
    }

    val overallAccuracy = if (totalReviews > 0) {
        ((totalSuccess.toFloat() / totalReviews.toFloat()) * 100).toInt()
    } else {
        0
    }

    val goals = listOf(5, 10, 15, 20, 30)

    val reminderTimes = listOf(
        Pair(8, 0) to "08:00",
        Pair(12, 0) to "12:00",
        Pair(18, 0) to "18:00",
        Pair(20, 0) to "20:00",
        Pair(22, 0) to "22:00"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Desempenho & Configurações", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Desempenho & Estatísticas (Room SQLite)
            Text(
                text = "📊 Seu Desempenho (SQLite)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Cartões Totais",
                    value = "$totalCards",
                    icon = Icons.Default.Quiz,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Revisões Feitas",
                    value = "$totalReviews",
                    icon = Icons.Default.BarChart,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Taxa de Acertos",
                    value = "$overallAccuracy%",
                    icon = Icons.Default.TrackChanges,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Ofensiva (Streak)",
                    value = "${userPreferences.streakCount} dias",
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Section 2: DataStore Preferences
            Text(
                text = "⚙️ Preferências do App (DataStore)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tema Escuro",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Alterna entre modo claro e escuro",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = userPreferences.isDarkMode,
                            onCheckedChange = onToggleDarkMode
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily Card Goal
                    Text(
                        text = "Meta Diária de Cartões para Revisar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Atualmente: ${userPreferences.dailyCardGoal} cartões/dia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        goals.forEachIndexed { index, goal ->
                            SegmentedButton(
                                selected = userPreferences.dailyCardGoal == goal,
                                onClick = { onSetDailyGoal(goal) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = goals.size)
                            ) {
                                Text("$goal")
                            }
                        }
                    }
                }
            }

            // Section 3: Notificações Locais
            Text(
                text = "🔔 Notificações Locais (AlarmManager)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Lembrete Diário de Estudo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Notificação diária no horário agendado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = userPreferences.isReminderEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateReminderSettings(
                                    enabled,
                                    userPreferences.reminderHour,
                                    userPreferences.reminderMinute
                                )
                            }
                        )
                    }

                    if (userPreferences.isReminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Horário do Lembrete:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            reminderTimes.forEachIndexed { index, pair ->
                                val (timePair, timeLabel) = pair
                                val isSelected = userPreferences.reminderHour == timePair.first && userPreferences.reminderMinute == timePair.second
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = {
                                        onUpdateReminderSettings(true, timePair.first, timePair.second)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = reminderTimes.size)
                                ) {
                                    Text(timeLabel)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onTestNotification()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testar Notificação Agora")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
