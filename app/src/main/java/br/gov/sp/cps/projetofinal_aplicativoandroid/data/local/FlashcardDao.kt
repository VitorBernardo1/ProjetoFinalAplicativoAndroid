package br.gov.sp.cps.projetofinal_aplicativoandroid.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Transaction
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getDecksWithCards(): Flow<List<DeckWithCards>>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckWithCardsById(deckId: Long): Flow<DeckWithCards?>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Long): DeckEntity?

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity): Int

    @Delete
    suspend fun deleteDeck(deck: DeckEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity): Int

    @Delete
    suspend fun deleteFlashcard(card: FlashcardEntity): Int

    @Query("SELECT COUNT(*) FROM flashcards")
    fun getTotalCardsCount(): Flow<Int>

    @Query("SELECT SUM(reviewCount) FROM flashcards")
    fun getTotalReviewsCount(): Flow<Int?>

    @Query("SELECT SUM(successCount) FROM flashcards")
    fun getTotalSuccessCount(): Flow<Int?>
}
