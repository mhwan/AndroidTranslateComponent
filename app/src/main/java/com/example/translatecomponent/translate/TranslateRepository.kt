package com.example.translatecomponent.translate

interface TranslateRepository {
    suspend fun translate(
        source: LangType,
        target: LangType,
        text: String
    ): TranslateResponse

    suspend fun detectLanguage(text: String): DetectLanguageResponse
}