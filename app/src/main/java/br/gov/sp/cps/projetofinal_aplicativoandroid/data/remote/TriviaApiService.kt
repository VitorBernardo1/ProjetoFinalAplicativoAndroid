package br.gov.sp.cps.projetofinal_aplicativoandroid.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApiService {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int = 10,
        @Query("category") categoryId: Int? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("type") type: String = "multiple"
    ): TriviaResponse

    @GET("api_category.php")
    suspend fun getCategories(): TriviaCategoryResponse

    companion object {
        private const val BASE_URL = "https://opentdb.com/"

        val instance: TriviaApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TriviaApiService::class.java)
        }
    }
}
