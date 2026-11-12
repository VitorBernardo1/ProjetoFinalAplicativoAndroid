package br.gov.sp.cps.projetofinal_aplicativoandroid.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenRoute(val route: String, val title: String, val icon: ImageVector? = null) {
    object Decks : ScreenRoute("decks", "Baralhos", Icons.AutoMirrored.Filled.MenuBook)
    object ExploreApi : ScreenRoute("explore_api", "Explorar API", Icons.Default.Explore)
    object StatsSettings : ScreenRoute("stats_settings", "Desempenho", Icons.Default.BarChart)

    object DeckDetail : ScreenRoute("deck_detail/{deckId}", "Detalhes") {
        fun createRoute(deckId: Long) = "deck_detail/$deckId"
    }

    object Quiz : ScreenRoute("quiz/{deckId}", "Quiz") {
        fun createRoute(deckId: Long) = "quiz/$deckId"
    }
}
