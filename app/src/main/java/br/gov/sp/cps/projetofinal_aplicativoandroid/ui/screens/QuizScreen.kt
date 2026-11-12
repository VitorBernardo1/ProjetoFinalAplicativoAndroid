package br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.DeckWithCards
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.local.FlashcardEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class QuizMode {
    FLIP_CARD,
    MULTIPLE_CHOICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    deckWithCards: DeckWithCards?,
    onNavigateBack: () -> Unit,
    onRecordResult: (FlashcardEntity, Boolean) -> Unit
) {
    if (deckWithCards == null || deckWithCards.cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sem cartões disponíveis para estudo.")
        }
        return
    }

    val cards = deckWithCards.cards
    var currentIndex by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf(QuizMode.FLIP_CARD) }
    var isFlipped by remember { mutableStateOf(false) }

    // Session stats
    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    // Multiple Choice selection state
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var hasAnsweredMCQ by remember { mutableStateOf(false) }

    val currentCard = if (currentIndex < cards.size) cards[currentIndex] else null

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlipAnimation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deckWithCards.deck.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFinished) {
                // Session Finished Summary View
                QuizSummaryView(
                    totalCards = cards.size,
                    correctCount = correctCount,
                    wrongCount = wrongCount,
                    onRestart = {
                        currentIndex = 0
                        correctCount = 0
                        wrongCount = 0
                        isFinished = false
                        isFlipped = false
                        selectedOptionIndex = -1
                        hasAnsweredMCQ = false
                    },
                    onFinish = onNavigateBack
                )
            } else if (currentCard != null) {
                // Mode selector
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    SegmentedButton(
                        selected = mode == QuizMode.FLIP_CARD,
                        onClick = {
                            mode = QuizMode.FLIP_CARD
                            isFlipped = false
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Cartão (Flip 3D)")
                    }
                    SegmentedButton(
                        selected = mode == QuizMode.MULTIPLE_CHOICE,
                        onClick = {
                            mode = QuizMode.MULTIPLE_CHOICE
                            selectedOptionIndex = -1
                            hasAnsweredMCQ = false
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Múltipla Escolha")
                    }
                }

                // Progress Bar
                val progress = ((currentIndex + 1).toFloat() / cards.size.toFloat())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cartão ${currentIndex + 1} de ${cards.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (mode == QuizMode.FLIP_CARD) {
                    // FLIP CARD VIEW
                    FlipCardView(
                        card = currentCard,
                        rotation = rotation,
                        isFlipped = isFlipped,
                        onFlip = { isFlipped = !isFlipped }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = if (!isFlipped) "Toque no cartão para revelar a resposta" else "Avalie o seu desempenho:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                onRecordResult(currentCard, false)
                                wrongCount++
                                if (currentIndex + 1 < cards.size) {
                                    currentIndex++
                                    isFlipped = false
                                } else {
                                    isFinished = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Errei ❌", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onRecordResult(currentCard, true)
                                correctCount++
                                if (currentIndex + 1 < cards.size) {
                                    currentIndex++
                                    isFlipped = false
                                } else {
                                    isFinished = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Acertei ✅", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // MULTIPLE CHOICE VIEW
                    MultipleChoiceView(
                        card = currentCard,
                        selectedIndex = selectedOptionIndex,
                        hasAnswered = hasAnsweredMCQ,
                        onOptionSelected = { idx ->
                            if (!hasAnsweredMCQ) {
                                selectedOptionIndex = idx
                                hasAnsweredMCQ = true
                                val optionsList = parseOptions(currentCard.optionsJson, currentCard.answer)
                                val isCorrect = if (currentCard.correctOptionIndex >= 0 && currentCard.correctOptionIndex < optionsList.size) {
                                    idx == currentCard.correctOptionIndex
                                } else {
                                    optionsList.getOrNull(idx) == currentCard.answer
                                }

                                onRecordResult(currentCard, isCorrect)
                                if (isCorrect) correctCount++ else wrongCount++
                            }
                        },
                        onNext = {
                            if (currentIndex + 1 < cards.size) {
                                currentIndex++
                                selectedOptionIndex = -1
                                hasAnsweredMCQ = false
                            } else {
                                isFinished = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FlipCardView(
    card: FlashcardEntity,
    rotation: Float,
    isFlipped: Boolean,
    onFlip: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isFlipped) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front Side (Question)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PERGUNTA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                // Back Side (Answer - mirrored back to read normally)
                Column(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RESPOSTA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceView(
    card: FlashcardEntity,
    selectedIndex: Int,
    hasAnswered: Boolean,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val optionsList: List<String> = remember(card.optionsJson, card.answer) {
        parseOptions(card.optionsJson, card.answer)
    }

    val correctIdx = if (card.correctOptionIndex >= 0 && card.correctOptionIndex < optionsList.size) {
        card.correctOptionIndex
    } else {
        optionsList.indexOf(card.answer).takeIf { it >= 0 } ?: 0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pergunta:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        optionsList.forEachIndexed { index, optionText ->
            val isCorrectOption = index == correctIdx
            val isSelectedOption = index == selectedIndex

            val containerColor = when {
                hasAnswered && isCorrectOption -> Color(0xFF2E7D32) // Green for correct
                hasAnswered && isSelectedOption && !isCorrectOption -> MaterialTheme.colorScheme.error // Red for wrong selected
                isSelectedOption -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val contentColor = when {
                hasAnswered && (isCorrectOption || (isSelectedOption && !isCorrectOption)) -> Color.White
                isSelectedOption -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable(enabled = !hasAnswered) { onOptionSelected(index) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = contentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${'A' + index}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (hasAnswered) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Próxima Pergunta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuizSummaryView(
    totalCards: Int,
    correctCount: Int,
    wrongCount: Int,
    onRestart: () -> Unit,
    onFinish: () -> Unit
) {
    val accuracy = if (totalCards > 0) ((correctCount.toFloat() / totalCards.toFloat()) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 Quiz Concluído!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aproveitamento: $accuracy%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✅ Acertos", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "$correctCount",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "❌ Erros", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "$wrongCount",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🔥 Sua ofensiva diária foi atualizada com sucesso!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refazer")
            }

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Concluir")
            }
        }
    }
}

private fun parseOptions(optionsJson: String?, answer: String): List<String> {
    if (!optionsJson.isNullOrEmpty()) {
        try {
            val type = object : TypeToken<List<String>>() {}.type
            val parsed: List<String> = Gson().fromJson(optionsJson, type)
            if (parsed.isNotEmpty()) return parsed
        } catch (e: Exception) {
            // fallback below
        }
    }
    // Fallback options if none provided
    return listOf(answer, "Outra alternativa A", "Outra alternativa B", "Outra alternativa C").shuffled()
}
