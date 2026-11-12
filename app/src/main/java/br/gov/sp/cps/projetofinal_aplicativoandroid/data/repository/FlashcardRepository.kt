package br.gov.sp.cps.projetofinal_aplicativoandroid.data.repository

import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.DeckEntity
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.DeckWithCards
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.FlashcardDao
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.FlashcardEntity
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote.TriviaApiService
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote.TriviaCategory
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote.TriviaQuestionResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(
    private val dao: FlashcardDao,
    private val apiService: TriviaApiService = TriviaApiService.instance
) {
    private val gson = Gson()

    fun getAllDecksWithCards(): Flow<List<DeckWithCards>> = dao.getDecksWithCards()

    fun getDeckWithCardsById(deckId: Long): Flow<DeckWithCards?> = dao.getDeckWithCardsById(deckId)

    suspend fun getDeckById(deckId: Long): DeckEntity? = dao.getDeckById(deckId)

    fun getCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>> = dao.getCardsForDeck(deckId)

    suspend fun createDeck(name: String, description: String, category: String): Long {
        return dao.insertDeck(
            DeckEntity(
                name = name,
                description = description,
                category = category
            )
        )
    }

    suspend fun updateDeck(deck: DeckEntity) {
        dao.updateDeck(deck)
    }

    suspend fun deleteDeck(deck: DeckEntity) {
        dao.deleteDeck(deck)
    }

    suspend fun addFlashcard(
        deckId: Long,
        question: String,
        answer: String,
        options: List<String>? = null,
        correctOptionIndex: Int = -1
    ): Long {
        val optionsJson = options?.let { gson.toJson(it) }
        return dao.insertFlashcard(
            FlashcardEntity(
                deckId = deckId,
                question = question,
                answer = answer,
                optionsJson = optionsJson,
                correctOptionIndex = correctOptionIndex
            )
        )
    }

    suspend fun updateFlashcard(card: FlashcardEntity) {
        dao.updateFlashcard(card)
    }

    suspend fun recordCardResult(card: FlashcardEntity, isSuccess: Boolean) {
        val updatedCard = card.copy(
            reviewCount = card.reviewCount + 1,
            successCount = if (isSuccess) card.successCount + 1 else card.successCount,
            errorCount = if (!isSuccess) card.errorCount + 1 else card.errorCount,
            lastReviewedAt = System.currentTimeMillis()
        )
        dao.updateFlashcard(updatedCard)
    }

    suspend fun deleteFlashcard(card: FlashcardEntity) {
        dao.deleteFlashcard(card)
    }

    // Remote REST API
    suspend fun fetchOnlineTriviaQuestions(
        amount: Int = 10,
        categoryId: Int? = null,
        difficulty: String? = null
    ): List<TriviaQuestionResult> {
        val response = apiService.getQuestions(
            amount = amount,
            categoryId = categoryId,
            difficulty = if (difficulty == "Qualquer") null else difficulty?.lowercase()
        )
        return response.results
    }

    suspend fun fetchOnlineCategories(): List<TriviaCategory> {
        return try {
            val response = apiService.getCategories()
            response.categories
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun importTriviaQuestionsAsDeck(
        deckName: String,
        description: String,
        categoryName: String,
        questions: List<TriviaQuestionResult>
    ): Long {
        val deckId = dao.insertDeck(
            DeckEntity(
                name = deckName,
                description = description,
                category = categoryName
            )
        )

        val cards = questions.map { q ->
            val options = q.allDecodedOptions
            val correctIdx = options.indexOf(q.decodedCorrectAnswer)
            FlashcardEntity(
                deckId = deckId,
                question = q.decodedQuestion,
                answer = q.decodedCorrectAnswer,
                optionsJson = gson.toJson(options),
                correctOptionIndex = correctIdx
            )
        }

        dao.insertFlashcards(cards)
        return deckId
    }

    fun getTotalCardsCount(): Flow<Int> = dao.getTotalCardsCount()
    fun getTotalReviewsCount(): Flow<Int?> = dao.getTotalReviewsCount()
    fun getTotalSuccessCount(): Flow<Int?> = dao.getTotalSuccessCount()
}
