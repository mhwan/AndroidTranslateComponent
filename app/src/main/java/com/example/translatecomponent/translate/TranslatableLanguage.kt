package com.example.translatecomponent.translate

data class TranslatableLanguage(val langType: LangType){
    override fun toString(): String {
        return LanguageUtil.convertLanguage(langType)
    }
}