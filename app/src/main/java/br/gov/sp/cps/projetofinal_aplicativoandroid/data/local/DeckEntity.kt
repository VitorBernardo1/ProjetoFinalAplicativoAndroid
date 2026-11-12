package br.gov.sp.cps.projetofinal_aplicativoandroid.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)
