package com.example.translatecomponent.translate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.Spanned
import android.text.method.ScrollingMovementMethod
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.translatecomponent.R
import com.example.translatecomponent.databinding.ActivityTranslateBinding
import com.example.translatecomponent.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TranslateActivity : AppCompatActivity() {
    private val viewModel: TranslateViewModel by viewModels()
    private val binding by lazy { ActivityTranslateBinding.inflate(LayoutInflater.from(this)) }
    private val invalidColor by lazy { ContextCompat.getColor(baseContext, R.color.sub_title_text) }
    private val validColor by lazy { ContextCompat.getColor(baseContext, R.color.title_text) }

    private var isTranslated = false

    private val ttsBundle: Bundle =
        Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null) }
    private lateinit var tts: TextToSpeech
    private val ttsBackgroundColorSpan by lazy {
        BackgroundColorSpan(ContextCompat.getColor(baseContext, R.color.se_primary_color_20))
    }
    private val ttsSpannable: Spannable
        get() = binding.layoutTranslate.tvTargetText.text as Spannable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initTTS()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detectLanguageCoroutine(this)
                translateTextCoroutine(this)
            }
        }
    }

    private fun detectLanguageCoroutine(coroutineScope: CoroutineScope) = coroutineScope.launch {
        viewModel.detectedLanguage.collect {
            val idx = when (it) {
                is Error -> {
                    binding.layoutTranslate.etSourceText.hint = "언어를 감지하지 못했습니다."
                    0
                }
                is DetectLanguageResponse.Success ->
                    LanguageUtil.mapTranslatableLanguageIndex(it.langCode)
            }
            binding.layoutTranslate.spinnerSourceLang.setSelection(idx)
        }
    }

    private fun translateTextCoroutine(coroutineScope: CoroutineScope) = coroutineScope.launch {
        viewModel.translateText.collect {
            when (it) {
                is Error -> {
                    toast(it.errorMessage)
                    binding.layoutTranslate.tvTargetText.apply {
                        isTranslated = false
                        setText("번역 결과가 없습니다.", TextView.BufferType.SPANNABLE)
                        setTextColor(invalidColor)
                        isSelected = false
                    }
                }
                is TranslateResponse.Success -> {
                    binding.layoutTranslate.tvTargetText.apply {
                        isTranslated = true
                        setText(it.message.result.translatedText, TextView.BufferType.SPANNABLE)
                        setTextColor(validColor)
                        isSelected = true
                    }
                    hideKeyboard()
                }
            }
        }
    }

    private fun initView() {
        createSpinnerAdapter(LanguageUtil.sourceLanguageList).also {
            binding.layoutTranslate.spinnerSourceLang.adapter = it
        }

        createSpinnerAdapter(LanguageUtil.targetLanguageList).also {
            binding.layoutTranslate.spinnerTargetLang.adapter = it
        }

        binding.layoutTranslate.tvTargetText.movementMethod = ScrollingMovementMethod()

        binding.layoutTranslate.etSourceText.doAfterTextChanged {
            viewModel.detectLanguage(it.toString())
        }

        binding.toolbar.btnWrite.setOnClickListener {
            setTranslateResult(
                getSourceLangType(),
                getSourceText(),
                getTargetLangType(),
                getTargetText()
            )
        }

        binding.layoutTranslate.btnTranslate.setOnClickListener {
            viewModel.translateText(
                getSourceLangType(),
                getTargetLangType(),
                getSourceText()
            )
        }

        binding.layoutTranslate.btnSpeak.setOnClickListener {
            val speakText = binding.layoutTranslate.tvTargetText.text.toString()
            if (speakText.isNotEmpty() && isTranslated) {
                val result = setTTSLanguage(getTargetLangType())
                if (result >= TextToSpeech.LANG_AVAILABLE) {
                    startPlay(speakText)
                } else {
                    toast("해당 언어는 읽을 수 없습니다.")
                }
            } else {
                toast("읽을 글자가 없습니다. 번역 후에 실행해주세요.")
            }
        }
    }

    private fun getSourceText() = binding.layoutTranslate.etSourceText.text.toString()
    private fun getTargetText() = binding.layoutTranslate.tvTargetText.text.toString()
    private fun getSourceLangType() =
        LanguageUtil.sourceLanguageList[binding.layoutTranslate.spinnerSourceLang.selectedItemPosition].langType

    private fun getTargetLangType() =
        LanguageUtil.targetLanguageList[binding.layoutTranslate.spinnerTargetLang.selectedItemPosition].langType

    private fun setTranslateResult(
        sourceLangType: LangType,
        sourceText: String,
        targetLangType: LangType,
        targetText: String
    ) {
        if (isTranslated) {
            Bundle().apply {
                putString(EXTRA_SOURCE_LANG, LanguageUtil.convertLanguage(sourceLangType))
                putString(EXTRA_SOURCE_TEXT, sourceText)
                putString(EXTRA_TARGET_LANG, LanguageUtil.convertLanguage(targetLangType))
                putString(EXTRA_TARGET_TEXT, targetText)
            }.also {
                setResult(RESULT_TRANSLATE_OK, Intent().putExtras(it))
            }
        }
        finish()
    }

    private fun createSpinnerAdapter(list: List<TranslatableLanguage>) = ArrayAdapter(
        baseContext,
        R.layout.item_spinner,
        list
    ).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun initTTS() {
        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                setTTSLanguage(LangType.KO)
            } else {
                toast("tts 초기화에 실패했습니다.")
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    clearHighlightText()
                }

                override fun onError(utteranceId: String?) {
                    toast("읽기 중 에러가 발생했습니다.")
                }

                override fun onRangeStart(
                    utteranceId: String?,
                    start: Int,
                    end: Int,
                    frame: Int
                ) {
                    showHighlightText(start, end)
                }
            })
        }
    }

    private fun showHighlightText(start: Int, end: Int) {
        runOnUiThread {
            ttsSpannable.setSpan(
                ttsBackgroundColorSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun clearHighlightText() {
        val start = ttsSpannable.getSpanStart(ttsBackgroundColorSpan)
        val end = ttsSpannable.getSpanEnd(ttsBackgroundColorSpan)
        if (start >= 0 && end >= 0) {
            ttsSpannable.removeSpan(ttsBackgroundColorSpan)
        }
    }

    private fun setTTSLanguage(langType: LangType) =
        tts.setLanguage(LanguageUtil.convertToLocale(langType))

    private fun startPlay(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsBundle, text)
    }

    private fun stopPlay() {
        tts.stop()
        clearHighlightText()
    }

    override fun onStop() {
        stopPlay()
        super.onStop()
    }

    override fun onDestroy() {
        stopPlay()
        tts.shutdown()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_SOURCE_LANG = "source_lang"
        const val EXTRA_SOURCE_TEXT = "source_text"
        const val EXTRA_TARGET_LANG = "target_lang"
        const val EXTRA_TARGET_TEXT = "target_text"
        const val RESULT_TRANSLATE_OK = 0x102
    }
}