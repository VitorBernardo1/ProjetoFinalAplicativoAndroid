package br.gov.sp.cps.projetofinal_aplicativoandroid.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val question: String,
    val answer: String,
    val optionsJson: String? = null,
    val correctOptionIndex: Int = -1,
    val reviewCount: Int = 0,
    val successCount: Int = 0,
    val errorCount: Int = 0,
    val lastReviewedAt: Long? = null
)
