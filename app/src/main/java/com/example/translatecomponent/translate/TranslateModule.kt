package com.example.translatecomponent.translate

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class TranslateModule {
    @ViewModelScoped
    @Binds
    abstract fun bindTranslateRepository(
        translateRepositoryImpl: TranslateRepositoryImpl
    ): TranslateRepository
}