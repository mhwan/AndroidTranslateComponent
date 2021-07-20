package com.example.translatecomponent.translate

data class Message(
    var result: Result
)

data class Result(
    var srcLangType: String,
    var tarLangType: String,
    var translatedText: String
)

sealed class Errors : TranslateResponse, DetectLanguageResponse
data class Error(var errorCode: String = "400", var errorMessage: String = "") : Errors()

sealed interface TranslateResponse {
    data class Success(var message: Message) : TranslateResponse
}

sealed interface DetectLanguageResponse {
    data class Success(var langCode: String) : DetectLanguageResponse
}