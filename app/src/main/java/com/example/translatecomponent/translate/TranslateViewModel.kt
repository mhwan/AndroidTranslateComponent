package com.example.translatecomponent.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val translateRepository: TranslateRepository
) : ViewModel() {
    private val _detectedLanguage: MutableStateFlow<DetectLanguageResponse> =
        MutableStateFlow(DetectLanguageResponse.Success(LangType.UNK.lang))
    private val _translateText: MutableSharedFlow<TranslateResponse> =
        MutableSharedFlow(1)
    val detectedLanguage: StateFlow<DetectLanguageResponse>
        get() = _detectedLanguage
    val translateText: SharedFlow<TranslateResponse>
        get() = _translateText

    fun translateText(
        source: LangType,
        target: LangType,
        text: String
    ) {
        viewModelScope.launch {
            if (!isValidTextLength(text)) {
                _translateText.emit(
                    Error(errorMessage = "0자 이상 5000자 이하의 글자를 입력해주세요.")
                )
                return@launch
            }

            if (!isValidSourceWithTarget(source, target)) {
                _translateText.emit(
                    Error(errorMessage = "해당 언어로 번역할 수 없습니다.")
                )
                return@launch
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    translateRepository.translate(
                        source,
                        target,
                        text
                    )
                }
                _translateText.emit(response)
            } catch (e: Exception) {
                _translateText.emit(
                    Error(errorMessage = "예상치 못한 오류가 발생했습니다.")
                )
            }
        }
    }

    private fun isValidSourceWithTarget(source: LangType, target: LangType) =
        LanguageUtil.canTranslate(source, target)

    private fun isValidTextLength(text: String) = text.isNotEmpty() && text.length <= 5000

    fun detectLanguage(text: String) {
        viewModelScope.launch {
            if (!isValidTextLength(text)) {
                _detectedLanguage.emit(
                    Error(errorMessage = "0자 이상 5000자 이하의 글자를 입력해주세요.")
                )
                return@launch
            }

            try {
                val result = withContext(Dispatchers.IO) {
                    translateRepository.detectLanguage(text)
                }
                _detectedLanguage.emit(result)
            } catch (e: Exception) {
                _detectedLanguage.emit(
                    Error(errorMessage = "예상치 못한 오류가 발생했습니다.")
                )
            }
        }
    }
}