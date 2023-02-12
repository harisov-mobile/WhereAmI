package ru.internetcloud.whereami.di

import dagger.Binds
import dagger.Module
import ru.internetcloud.whereami.data.repository.MapRepositoryImpl
import ru.internetcloud.whereami.domain.MapRepository

@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindMapRepository(impl: MapRepositoryImpl): MapRepository

    companion object {
    }
}
