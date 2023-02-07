package ru.internetcloud.wereami.di

import dagger.Binds
import dagger.Module
import ru.internetcloud.wereami.data.repository.MapRepositoryImpl
import ru.internetcloud.wereami.domain.MapRepository

@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindMapRepository(impl: MapRepositoryImpl): MapRepository

    companion object {

    }

}

