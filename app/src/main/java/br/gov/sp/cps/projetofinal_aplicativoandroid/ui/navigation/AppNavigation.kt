package br.gov.sp.cps.projetofinal_aplicativoandroid.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens.DeckDetailScreen
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens.DecksScreen
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens.ExploreApiScreen
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens.QuizScreen
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens.StatsSettingsScreen
import br.gov.sp.cps.projetofinal_aplicativoandroid.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()

    val decksWithCards by viewModel.decksWithCards.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val totalCards by viewModel.totalCardsCount.collectAsState()
    val totalReviews by viewModel.totalReviewsCount.collectAsState()
    val totalSuccess by viewModel.totalSuccessCount.collectAsState()

    val onlineCategories by viewModel.onlineCategories.collectAsState()
    val onlineQuestions by viewModel.onlineQuestions.collectAsState()
    val isLoadingOnline by viewModel.isLoadingOnline.collectAsState()
    val apiError by viewModel.apiError.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        ScreenRoute.Decks,
        ScreenRoute.ExploreApi,
        ScreenRoute.StatsSettings
    )

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.route == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                item.icon?.let { Icon(it, contentDescription = item.title) }
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Decks.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen 1: Decks List (Home)
            composable(ScreenRoute.Decks.route) {
                DecksScreen(
                    decksWithCards = decksWithCards,
                    userPreferences = userPreferences,
                    onNavigateToDeckDetail = { deckId ->
                        navController.navigate(ScreenRoute.DeckDetail.createRoute(deckId))
                    },
                    onNavigateToQuiz = { deckId ->
                        navController.navigate(ScreenRoute.Quiz.createRoute(deckId))
                    },
                    onCreateDeck = { name, desc, cat ->
                        viewModel.createDeck(name, desc, cat)
                    },
                    onDeleteDeck = { deck ->
                        viewModel.deleteDeck(deck)
                    }
                )
            }

            // Screen 2: Deck Detail & Cards Management
            composable(
                route = ScreenRoute.DeckDetail.route,
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getLong("deckId") ?: -1L
                val deckWithCards = decksWithCards.find { it.deck.id == deckId }

                DeckDetailScreen(
                    deckWithCards = deckWithCards,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQuiz = { id ->
                        navController.navigate(ScreenRoute.Quiz.createRoute(id))
                    },
                    onAddFlashcard = { dId, q, a, opts, cIdx ->
                        viewModel.addFlashcard(dId, q, a, opts, cIdx)
                    },
                    onDeleteFlashcard = { card ->
                        viewModel.deleteFlashcard(card)
                    }
                )
            }

            // Screen 3: Flashcard Quiz & Flip Screen
            composable(
                route = ScreenRoute.Quiz.route,
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getLong("deckId") ?: -1L
                val deckWithCards = decksWithCards.find { it.deck.id == deckId }

                QuizScreen(
                    deckWithCards = deckWithCards,
                    onNavigateBack = { navController.popBackStack() },
                    onRecordResult = { card, isSuccess ->
                        viewModel.recordQuizAnswer(card, isSuccess)
                    }
                )
            }

            // Screen 4: Explore REST API (Open Trivia DB)
            composable(ScreenRoute.ExploreApi.route) {
                ExploreApiScreen(
                    categories = onlineCategories,
                    questions = onlineQuestions,
                    isLoading = isLoadingOnline,
                    errorMessage = apiError,
                    onFetchQuestions = { amt, catId, diff ->
                        viewModel.fetchTriviaQuestions(amt, catId, diff)
                    },
                    onImportAsDeck = { dName, desc, cName, qList ->
                        viewModel.importTriviaQuestionsAsDeck(dName, desc, cName, qList) {
                            // On import success
                        }
                    },
                    onImportSuccess = {
                        navController.navigate(ScreenRoute.Decks.route) {
                            popUpTo(ScreenRoute.Decks.route) { inclusive = true }
                        }
                    }
                )
            }

            // Screen 5: Statistics & Settings (DataStore + Notifications)
            composable(ScreenRoute.StatsSettings.route) {
                StatsSettingsScreen(
                    userPreferences = userPreferences,
                    totalCards = totalCards,
                    totalReviews = totalReviews ?: 0,
                    totalSuccess = totalSuccess ?: 0,
                    onToggleDarkMode = { enabled ->
                        viewModel.setDarkMode(enabled)
                    },
                    onSetDailyGoal = { goal ->
                        viewModel.setDailyGoal(goal)
                    },
                    onUpdateReminderSettings = { enabled, h, m ->
                        viewModel.updateReminderSettings(enabled, h, m)
                    },
                    onTestNotification = {
                        viewModel.triggerTestNotification()
                    }
                )
            }
        }
    }
}
