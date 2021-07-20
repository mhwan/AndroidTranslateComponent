package com.example.translatecomponent.translate

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val CLIENT_ID_HEADER = "X-Naver-Client-Id"
    private const val CLIENT_SECRET_HEADER = "X-Naver-Client-Secret"
    private const val CLIENT_ID_VALUE = "MIn7k13NI0s3qdm8V2xn"
    private const val CLIENT_SECRET_VALUE = "9HOO12qTV4"
    private const val BASE_URL_NAVER_OPEN_API = "https://openapi.naver.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_NAVER_OPEN_API)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val original = chain.request()
                chain.proceed(original.newBuilder().apply {
                    addHeader(CLIENT_ID_HEADER, CLIENT_ID_VALUE)
                    addHeader(
                        CLIENT_SECRET_HEADER,
                        CLIENT_SECRET_VALUE
                    )
                }.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    @Singleton
    @Provides
    fun provideTranslateApi(): PapagoService =
        retrofit.create(PapagoService::class.java)
}