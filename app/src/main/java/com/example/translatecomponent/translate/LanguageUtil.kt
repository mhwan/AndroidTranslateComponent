package com.example.translatecomponent.translate

import okhttp3.internal.toImmutableList
import java.util.*

object LanguageUtil {
    val targetLanguageList = listOf(
        TranslatableLanguage(LangType.KO),
        TranslatableLanguage(LangType.EN),
        TranslatableLanguage(LangType.JA),
        TranslatableLanguage(LangType.ZH_CN),
        TranslatableLanguage(LangType.ZH_TW),
        TranslatableLanguage(LangType.VI),
        TranslatableLanguage(LangType.TH),
        TranslatableLanguage(LangType.DE),
        TranslatableLanguage(LangType.RU),
        TranslatableLanguage(LangType.ES),
        TranslatableLanguage(LangType.IT),
        TranslatableLanguage(LangType.FR)
    )

    val sourceLanguageList
        get() = targetLanguageList.toMutableList().apply {
            add(0, TranslatableLanguage(LangType.UNK))
        }.toImmutableList()

    private val translatableMap = mapOf(
        LangType.KO to setOf(
            LangType.EN,
            LangType.JA,
            LangType.ZH_CN,
            LangType.ZH_TW,
            LangType.VI,
            LangType.ID,
            LangType.TH,
            LangType.DE,
            LangType.RU,
            LangType.ES,
            LangType.IT,
            LangType.FR
        ),
        LangType.EN to setOf(
            LangType.JA,
            LangType.FR,
            LangType.ZH_CN,
            LangType.ZH_TW,
            LangType.KO
        ),
        LangType.JA to setOf(
            LangType.ZH_CN,
            LangType.ZH_TW,
            LangType.KO,
            LangType.EN
        ),
        LangType.ZH_CN to setOf(
            LangType.ZH_TW,
            LangType.KO,
            LangType.EN,
            LangType.JA
        ),
        LangType.ZH_TW to setOf(
            LangType.KO,
            LangType.EN,
            LangType.JA,
            LangType.ZH_CN
        ),
        LangType.FR to setOf(
            LangType.KO,
            LangType.EN
        ),
        LangType.VI to setOf(LangType.KO),
        LangType.ID to setOf(LangType.KO),
        LangType.TH to setOf(LangType.KO),
        LangType.DE to setOf(LangType.KO),
        LangType.RU to setOf(LangType.KO),
        LangType.ES to setOf(LangType.KO),
        LangType.IT to setOf(LangType.KO)
    )

    fun convertLanguage(langType: LangType) = when (langType) {
        LangType.KO -> "?????????"
        LangType.EN -> "??????"
        LangType.ZH_CN -> "????????? ??????"
        LangType.ZH_TW -> "????????? ??????"
        LangType.ES -> "????????????"
        LangType.FR -> "????????????"
        LangType.VI -> "????????????"
        LangType.TH -> "?????????"
        LangType.ID -> "??????????????????"
        LangType.JA -> "?????????"
        LangType.HI -> "?????????"
        LangType.DE -> "?????????"
        LangType.PT -> "???????????????"
        LangType.FA -> "???????????????"
        LangType.AR -> "?????????"
        LangType.MM -> "????????????"
        LangType.RU -> "????????????"
        LangType.IT -> "???????????????"
        LangType.UNK -> "?????? ??????"
    }

    fun mapTranslatableLanguageIndex(langCode: String): Int {
        val langType: LangType = runCatching {
            LangType.valueOf(langCode.uppercase())
        }.onSuccess {
            it
        }.getOrDefault(LangType.UNK)

        val idx = sourceLanguageList.indexOf(TranslatableLanguage(langType))
        return if (idx > 0) idx else 0
    }

    fun convertToLocale(langType: LangType): Locale = when (langType) {
        LangType.KO -> Locale.KOREAN
        LangType.EN -> Locale.ENGLISH
        LangType.ZH_CN -> Locale.SIMPLIFIED_CHINESE
        LangType.ZH_TW -> Locale.TRADITIONAL_CHINESE
        LangType.JA -> Locale.JAPANESE
        LangType.DE -> Locale.GERMAN
        LangType.FR -> Locale.FRENCH
        LangType.IT -> Locale.ITALIAN
        else -> Locale(langType.lang.lowercase())
    }

    fun canTranslate(source: LangType, target: LangType) =
        translatableMap[source]?.contains(target) ?: false
}
