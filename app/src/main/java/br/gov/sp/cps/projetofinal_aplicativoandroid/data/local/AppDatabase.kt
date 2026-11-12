package br.gov.sp.cps.projetofinal_aplicativoandroid.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DeckEntity::class, FlashcardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "estudo_flash_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database.flashcardDao())
                    }
                }
            }
        }

        private suspend fun seedDatabase(dao: FlashcardDao) {
            // Seed initial deck 1: Inglês Básico
            val deck1Id = dao.insertDeck(
                DeckEntity(
                    name = "Inglês Básico",
                    description = "Vocabulário e expressões essenciais do dia a dia",
                    category = "Idiomas"
                )
            )
            dao.insertFlashcards(
                listOf(
                    FlashcardEntity(
                        deckId = deck1Id,
                        question = "Como se diz 'Obrigado' e 'De nada' em inglês?",
                        answer = "'Thank you' e 'You're welcome'",
                        optionsJson = "[\"'Thank you' e 'You're welcome'\", \"'Sorry' e 'Please'\", \"'Hello' e 'Goodbye'\", \"'Yes' e 'No'\"]",
                        correctOptionIndex = 0
                    ),
                    FlashcardEntity(
                        deckId = deck1Id,
                        question = "Qual o significado da palavra 'Achievement'?",
                        answer = "Conquista ou realização",
                        optionsJson = "[\"Conquista ou realização\", \"Desafio difícil\", \"Horário marcado\", \"Agradecimento\"]",
                        correctOptionIndex = 0
                    ),
                    FlashcardEntity(
                        deckId = deck1Id,
                        question = "Traduza: 'I would like a cup of coffee, please.'",
                        answer = "Eu gostaria de uma xícara de café, por favor.",
                        optionsJson = "[\"Eu gostaria de uma xícara de café, por favor.\", \"Eu quero um copo de água.\", \"Eu gosto muito de café.\", \"Por favor traga o cardápio.\"]",
                        correctOptionIndex = 0
                    )
                )
            )

            // Seed initial deck 2: Curiosidades Gerais
            val deck2Id = dao.insertDeck(
                DeckEntity(
                    name = "Curiosidades Gerais",
                    description = "Fatos fascinantes sobre a Terra e a Ciência",
                    category = "Conhecimentos Gerais"
                )
            )
            dao.insertFlashcards(
                listOf(
                    FlashcardEntity(
                        deckId = deck2Id,
                        question = "Qual é o maior planeta do Sistema Solar?",
                        answer = "Júpiter",
                        optionsJson = "[\"Júpiter\", \"Saturno\", \"Terra\", \"Marte\"]",
                        correctOptionIndex = 0
                    ),
                    FlashcardEntity(
                        deckId = deck2Id,
                        question = "Quantos elementos químicos existem na Tabela Periódica oficial?",
                        answer = "118 elementos",
                        optionsJson = "[\"118 elementos\", \"100 elementos\", \"150 elementos\", \"92 elementos\"]",
                        correctOptionIndex = 0
                    ),
                    FlashcardEntity(
                        deckId = deck2Id,
                        question = "Qual é o animal terrestre mais rápido do mundo?",
                        answer = "Guepardo (Chita)",
                        optionsJson = "[\"Guepardo (Chita)\", \"Leão\", \"Gnu\", \"Antílope\"]",
                        correctOptionIndex = 0
                    )
                )
            )

            // Seed initial deck 3: Kotlin & Android
            val deck3Id = dao.insertDeck(
                DeckEntity(
                    name = "Kotlin & Jetpack Compose",
                    description = "Conceitos de desenvolvimento Android moderno",
                    category = "Programação"
                )
            )
            dao.insertFlashcards(
                listOf(
                    FlashcardEntity(
                        deckId = deck3Id,
                        question = "Qual a principal diferença entre 'val' e 'var' em Kotlin?",
                        answer = "'val' é imutável (read-only) e 'var' é mutável.",
                        optionsJson = "[\"'val' é imutável (read-only) e 'var' é mutável.\", \"'var' é para inteiros e 'val' para strings.\", \"Ambos são idênticos.\", \"'val' é privado e 'var' é público.\"]",
                        correctOptionIndex = 0
                    ),
                    FlashcardEntity(
                        deckId = deck3Id,
                        question = "Para que serve a annotation @Composable em Jetpack Compose?",
                        answer = "Indica que a função descreve um componente de interface do usuário.",
                        optionsJson = "[\"Indica que a função descreve um componente de interface do usuário.\", \"Cria uma tabela no banco de dados.\", \"Executa uma chamada HTTP síncrona.\", \"Configura uma notificação de sistema.\"]",
                        correctOptionIndex = 0
                    ),
                    FlashcardEntity(
                        deckId = deck3Id,
                        question = "Qual biblioteca Android é usada para armazenar preferências com Flow?",
                        answer = "Preferences DataStore",
                        optionsJson = "[\"Preferences DataStore\", \"SharedPreferences Antigo\", \"SQLite Core\", \"FileIO Stream\"]",
                        correctOptionIndex = 0
                    )
                )
            )
        }
    }
}
