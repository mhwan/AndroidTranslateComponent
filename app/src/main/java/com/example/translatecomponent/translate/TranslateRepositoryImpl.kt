package com.example.translatecomponent.translate

import javax.inject.Inject

class TranslateRepositoryImpl @Inject constructor(
    private val translateDataSource: TranslateDataSource
) : TranslateRepository {

    override suspend fun translate(
        source: LangType,
        target: LangType,
        text: String
    ) = translateDataSource.translate(source.lang, target.lang, text)


    override suspend fun detectLanguage(text: String) = translateDataSource.detectLanguage(text)
}