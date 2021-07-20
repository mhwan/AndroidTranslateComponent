package com.example.translatecomponent.translate

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

object ErrorParser {
    private val gson = Gson()

    fun parseErrorResponse(responseBody: ResponseBody?): Error? {
        val type = object : TypeToken<Error>() {}.type
        return gson.fromJson(responseBody?.charStream(), type)
    }
}