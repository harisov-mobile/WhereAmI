package ru.internetcloud.wereami.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.internetcloud.wereami.presentation.map.MapViewModel

@Module
interface ViewModelModule {

    @IntoMap
    @ViewModelKey(MapViewModel::class)
    @Binds
    fun bindMapViewModel(impl: MapViewModel): ViewModel

}
