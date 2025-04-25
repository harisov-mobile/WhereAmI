package ru.internetcloud.whereami.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.internetcloud.whereami.data.repository.MapRepositoryImpl
import ru.internetcloud.whereami.domain.MapRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindMapRepository(impl: MapRepositoryImpl): MapRepository

    companion object {
    }
}

