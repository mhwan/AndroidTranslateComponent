package com.example.translatecomponent.translate

import retrofit2.Response
import retrofit2.http.*

interface PapagoService {
    @FormUrlEncoded
    @POST(POST_TRANSLATE_PARAMS)
    suspend fun translate(
        @Field("source") source: String,
        @Field("target") target: String,
        @Field("text") text: String
    ): Response<TranslateResponse.Success>

    @FormUrlEncoded
    @POST(POST_DETECT_LANG_TYPE_PARAMS)
    suspend fun detectLanguage(
        @Field("query") query: String
    ): Response<DetectLanguageResponse.Success>


    companion object {
        const val POST_TRANSLATE_PARAMS = "v1/papago/n2mt"
        const val POST_DETECT_LANG_TYPE_PARAMS = "v1/papago/detectLangs"
    }
}