package br.gov.sp.cps.projetofinal_aplicativoandroid.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.AppDatabase
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.DeckEntity
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.DeckWithCards
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.FlashcardEntity
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.preferences.UserPreferences
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.preferences.UserPreferencesRepository
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote.TriviaCategory
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote.TriviaQuestionResult
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.repository.FlashcardRepository
import br.gov.sp.cps.projetofinal_aplicativoandroid.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FlashcardRepository(database.flashcardDao())
    private val preferencesRepository = UserPreferencesRepository(application)

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences(
                isDarkMode = false,
                dailyCardGoal = 10,
                studiedTodayCount = 0,
                lastStudyDate = "",
                streakCount = 1,
                isReminderEnabled = true,
                reminderHour = 20,
                reminderMinute = 0
            )
        )

    val decksWithCards: StateFlow<List<DeckWithCards>> = repository.getAllDecksWithCards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalCardsCount: StateFlow<Int> = repository.getTotalCardsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalReviewsCount: StateFlow<Int?> = repository.getTotalReviewsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalSuccessCount: StateFlow<Int?> = repository.getTotalSuccessCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Online Trivia / API state
    private val _onlineCategories = MutableStateFlow<List<TriviaCategory>>(emptyList())
    val onlineCategories: StateFlow<List<TriviaCategory>> = _onlineCategories.asStateFlow()

    private val _onlineQuestions = MutableStateFlow<List<TriviaQuestionResult>>(emptyList())
    val onlineQuestions: StateFlow<List<TriviaQuestionResult>> = _onlineQuestions.asStateFlow()

    private val _isLoadingOnline = MutableStateFlow(false)
    val isLoadingOnline: StateFlow<Boolean> = _isLoadingOnline.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    init {
        fetchOnlineCategories()
    }

    // Deck actions
    fun createDeck(name: String, description: String, category: String) {
        viewModelScope.launch {
            repository.createDeck(name, description, category)
        }
    }

    fun updateDeck(deck: DeckEntity) {
        viewModelScope.launch {
            repository.updateDeck(deck)
        }
    }

    fun deleteDeck(deck: DeckEntity) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    // Flashcard actions
    fun addFlashcard(
        deckId: Long,
        question: String,
        answer: String,
        options: List<String>? = null,
        correctOptionIndex: Int = -1
    ) {
        viewModelScope.launch {
            repository.addFlashcard(deckId, question, answer, options, correctOptionIndex)
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
        }
    }

    fun recordQuizAnswer(card: FlashcardEntity, isSuccess: Boolean) {
        viewModelScope.launch {
            repository.recordCardResult(card, isSuccess)
            preferencesRepository.incrementStudiedToday(1)
        }
    }

    // Preferences actions
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            preferencesRepository.setDailyGoal(goal)
        }
    }

    fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setReminderSettings(enabled, hour, minute)
            if (enabled) {
                NotificationHelper.scheduleDailyReminder(getApplication(), hour, minute)
            } else {
                NotificationHelper.cancelReminder(getApplication())
            }
        }
    }

    fun triggerTestNotification() {
        val streak = userPreferences.value.streakCount
        NotificationHelper.showReminderNotification(getApplication(), streak)
    }

    // API REST actions
    fun fetchOnlineCategories() {
        viewModelScope.launch {
            try {
                val categories = repository.fetchOnlineCategories()
                _onlineCategories.value = categories
            } catch (e: Exception) {
                _onlineCategories.value = emptyList()
            }
        }
    }

    fun fetchTriviaQuestions(amount: Int = 10, categoryId: Int? = null, difficulty: String? = null) {
        viewModelScope.launch {
            _isLoadingOnline.value = true
            _apiError.value = null
            try {
                val results = repository.fetchOnlineTriviaQuestions(amount, categoryId, difficulty)
                _onlineQuestions.value = results
                if (results.isEmpty()) {
                    _apiError.value = "Nenhuma pergunta encontrada para os filtros selecionados."
                }
            } catch (e: Exception) {
                _apiError.value = "Falha ao conectar com a API: ${e.localizedMessage ?: "Erro de conexão"}"
                _onlineQuestions.value = emptyList()
            } finally {
                _isLoadingOnline.value = false
            }
        }
    }

    fun importTriviaQuestionsAsDeck(
        deckName: String,
        description: String,
        categoryName: String,
        questions: List<TriviaQuestionResult>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.importTriviaQuestionsAsDeck(deckName, description, categoryName, questions)
            onSuccess()
        }
    }
}
