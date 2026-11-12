package br.gov.sp.cps.projetofinal_aplicativoandroid.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote.TriviaCategory
import br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote.TriviaQuestionResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreApiScreen(
    categories: List<TriviaCategory>,
    questions: List<TriviaQuestionResult>,
    isLoading: Boolean,
    errorMessage: String?,
    onFetchQuestions: (amount: Int, categoryId: Int?, difficulty: String?) -> Unit,
    onImportAsDeck: (deckName: String, description: String, categoryName: String, questions: List<TriviaQuestionResult>) -> Unit,
    onImportSuccess: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TriviaCategory?>(null) }
    var selectedDifficulty by remember { mutableStateOf("Qualquer") }
    var selectedAmount by remember { mutableIntStateOf(10) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var difficultyDropdownExpanded by remember { mutableStateOf(false) }
    var amountDropdownExpanded by remember { mutableStateOf(false) }

    var showImportDialog by remember { mutableStateOf(false) }

    val difficulties = listOf("Qualquer", "Easy", "Medium", "Hard")
    val amounts = listOf(5, 10, 15, 20)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorar API REST", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Trivia Database API",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Baixe quizzes educativos e curiosidades gerais diretamente da nuvem para criar novos baralhos no seu aplicativo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filters Form
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Category Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Todas as Categorias",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria Online") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas as Categorias") },
                            onClick = {
                                selectedCategory = null
                                categoryDropdownExpanded = false
                            }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Difficulty Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = difficultyDropdownExpanded,
                        onExpandedChange = { difficultyDropdownExpanded = !difficultyDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedDifficulty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dificuldade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = difficultyDropdownExpanded,
                            onDismissRequest = { difficultyDropdownExpanded = false }
                        ) {
                            difficulties.forEach { diff ->
                                DropdownMenuItem(
                                    text = { Text(diff) },
                                    onClick = {
                                        selectedDifficulty = diff
                                        difficultyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = amountDropdownExpanded,
                        onExpandedChange = { amountDropdownExpanded = !amountDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "$selectedAmount perguntas",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Quantidade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = amountDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = amountDropdownExpanded,
                            onDismissRequest = { amountDropdownExpanded = false }
                        ) {
                            amounts.forEach { amt ->
                                DropdownMenuItem(
                                    text = { Text("$amt perguntas") },
                                    onClick = {
                                        selectedAmount = amt
                                        amountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        onFetchQuestions(selectedAmount, selectedCategory?.id, selectedDifficulty)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(24.dp).width(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscando na API...")
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar Perguntas (API REST)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (questions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resultados (${questions.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { showImportDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Importar Baralho")
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(questions) { q ->
                        TriviaQuestionPreviewCard(question = q)
                    }
                }
            } else if (!isLoading && errorMessage == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecione a categoria e clique no botão acima para buscar perguntas online via REST API.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        ImportTriviaDeckDialog(
            defaultCategory = selectedCategory?.name ?: "Curiosidades Online",
            questionsCount = questions.size,
            onDismiss = { showImportDialog = false },
            onConfirm = { deckName, description, categoryName ->
                onImportAsDeck(deckName, description, categoryName, questions)
                showImportDialog = false
                onImportSuccess()
            }
        )
    }
}

@Composable
fun TriviaQuestionPreviewCard(question: TriviaQuestionResult) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "${question.category} | ${question.difficulty.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question.decodedQuestion,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Resposta Correta: ${question.decodedCorrectAnswer}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ImportTriviaDeckDialog(
    defaultCategory: String,
    questionsCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (deckName: String, description: String, categoryName: String) -> Unit
) {
    var deckName by remember { mutableStateOf("Quiz - $defaultCategory") }
    var description by remember { mutableStateOf("Importado da Open Trivia DB ($questionsCount perguntas)") }
    var categoryName by remember { mutableStateOf(defaultCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importar como Baralho Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "As $questionsCount perguntas serão salvas permanentemente no banco SQLite (Room).",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Nome do Baralho *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Categoria *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deckName.isNotBlank() && categoryName.isNotBlank()) {
                        onConfirm(deckName.trim(), description.trim(), categoryName.trim())
                    }
                },
                enabled = deckName.isNotBlank() && categoryName.isNotBlank()
            ) {
                Text("Salvar Baralho")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
