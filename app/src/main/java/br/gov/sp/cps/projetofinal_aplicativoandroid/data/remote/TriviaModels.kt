package br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote

import android.os.Build
import android.text.Html
import com.google.gson.annotations.SerializedName

data class TriviaResponse(
    @SerializedName("response_code") val responseCode: Int,
    @SerializedName("results") val results: List<TriviaQuestionResult>
)

data class TriviaCategoryResponse(
    @SerializedName("trivia_categories") val categories: List<TriviaCategory>
)

data class TriviaCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class TriviaQuestionResult(
    @SerializedName("category") val category: String,
    @SerializedName("type") val type: String,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("question") val rawQuestion: String,
    @SerializedName("correct_answer") val rawCorrectAnswer: String,
    @SerializedName("incorrect_answers") val rawIncorrectAnswers: List<String>
) {
    val decodedQuestion: String
        get() = decodeHtml(rawQuestion)

    val decodedCorrectAnswer: String
        get() = decodeHtml(rawCorrectAnswer)

    val decodedIncorrectAnswers: List<String>
        get() = rawIncorrectAnswers.map { decodeHtml(it) }

    val allDecodedOptions: List<String>
        get() = (listOf(decodedCorrectAnswer) + decodedIncorrectAnswers).shuffled()

    private fun decodeHtml(text: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(text).toString()
            }
        } catch (e: Exception) {
            text.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
        }
    }
}
