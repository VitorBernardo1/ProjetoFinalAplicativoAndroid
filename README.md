# 🎴 Estudo Flash - Gerenciador de Micro-aprendizado e Quiz

**Estudo Flash** é um aplicativo Android desenvolvido em **Kotlin** e **Jetpack Compose** focado em resolver o problema da falta de tempo para estudar conteúdos extensos. Ele facilita revisões rápidas por meio de cartões de memória (*flashcards*), quizzes interativos e metas de estudos diárias.

---

## 🚀 Requisitos e Funcionalidades

### 1. 🗄️ SQLite (Room Database)
- Persistência local de dados para **Baralhos** e **Cartões de Memória (*Flashcards*)**.
- Armazena estatísticas de cada cartão (quantidade de revisões, acertos e erros).
- Inicialização automática com 3 baralhos pré-carregados:
  - *Inglês Básico*
  - *Curiosidades Gerais*
  - *Kotlin & Jetpack Compose*

### 2. 💾 DataStore (Preferências do Usuário)
- Persistência com `androidx.datastore:datastore-preferences`.
- Salva o estado do **Tema Escuro / Claro** (Dynamic Dark Mode).
- Armazena a **Meta Diária de Cartões** a revisar (ex: 5, 10, 15, 20 ou 30).
- Registra o progresso diário e mantém a **Ofensiva (*Streak*)** de dias consecutivos de estudo.
- Salva configurações de horário para lembretes locais.

### 3. 🌐 API REST (Consumo de Serviço Externo)
- Integração com a API pública **Open Trivia Database** via **Retrofit 2** e **Gson**.
- Permite buscar e filtrar perguntas da nuvem por categoria, dificuldade e quantidade.
- Decodificação automática de entidades HTML nas perguntas e respostas.
- **Importação em 1 clique:** converte as perguntas da API REST diretamente em baralhos locais no banco SQLite.

### 4. 🔔 Notificações Locais
- Agendamento diário via `AlarmManager`, `BroadcastReceiver` e `NotificationManager`.
- Exibe o status da sua **Ofensiva (*Streak*)** na notificação para incentivar a rotina de estudos.
- Suporte a permissões do Android 13+ (`POST_NOTIFICATIONS`).
- Inclui botão de teste imediato de notificações na aba de configurações.

### 5. 📱 Interface em Jetpack Compose & Navegação
- **Navegação Declarativa (`androidx.navigation:navigation-compose`)** com Bottom Navigation Bar.
- **Modos de Quiz Interativos:**
  - **Flashcard 3D Flip Card:** Animação gráfica de rotação 3D (`graphicsLayer`) para revelar a resposta no verso do cartão.
  - **Múltipla Escolha:** Pergunta com opções A, B, C, D e *feedback* de cor imediato (verde para acerto, vermelho para erro).
- Resumo ao final do Quiz exibindo taxa de aproveitamento (%).

---

## 🛠️ Arquitetura do Projeto

O projeto adota os padrões **MVVM (Model-View-ViewModel)** e **Repository Pattern**:

```
br.gov.sp.cps.projetofinal_aplicativoandroid/
├── data/
│   ├── local/            # Entidades Room, DAOs e AppDatabase (SQLite)
│   ├── preferences/      # UserPreferencesRepository (DataStore)
│   ├── remote/           # TriviaApiService e DTOs (Retrofit API REST)
│   └── repository/       # FlashcardRepository
├── notification/         # NotificationHelper e ReminderBroadcastReceiver
├── ui/
│   ├── navigation/       # ScreenRoute e AppNavigation (NavHost)
│   ├── screens/          # DecksScreen, DeckDetailScreen, QuizScreen, ExploreApiScreen, StatsSettingsScreen
│   ├── theme/            # Material 3 Theme, Colors, Type
│   └── viewmodel/        # MainViewModel
└── MainActivity.kt
```

---

## ⚙️ Como Executar o Projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/VitorBernardo1/ProjetoFinalAplicativoAndroid.git
   ```
2. Abra o projeto no **Android Studio** (Ladybug / Jellyfish ou superior).
3. Sincronize o projeto com os arquivos Gradle (`Sync Project with Gradle Files`).
4. Execute o aplicativo em um emulador Android ou dispositivo físico (Android 7.0 / SDK 24 ou superior).

---

## 👨‍💻 Autor
Desenvolvido por **Vitor Bernardo** ([@VitorBernardo1](https://github.com/VitorBernardo1)).
