package com.example.translatecomponent.translate

import javax.inject.Inject

class TranslateDataSource @Inject constructor(
    private val translateApi: PapagoService
) {
    suspend fun translate(source: String, target: String, text: String): TranslateResponse {
        val result = translateApi.translate(source, target, text)
        return if (result.isSuccessful) {
            result.body() ?: Error(errorMessage = "success but null pointer exception")
        } else {
            ErrorParser.parseErrorResponse(result.errorBody())
                ?: Error(errorMessage = "fail null pointer exception")
        }
    }

    suspend fun detectLanguage(query: String): DetectLanguageResponse {
        val result = translateApi.detectLanguage(query)
        return if (result.isSuccessful) {
            result.body() ?: Error(errorMessage = "success but null pointer exception")
        } else {
            ErrorParser.parseErrorResponse(result.errorBody())
                ?: Error(errorMessage = "fail null pointer exception")
        }
    }
}
