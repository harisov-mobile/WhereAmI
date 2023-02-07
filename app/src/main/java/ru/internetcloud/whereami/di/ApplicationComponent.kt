package ru.internetcloud.whereami.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import ru.internetcloud.whereami.presentation.map.MapFragment

@Component(modules = [DataModule::class, ViewModelModule::class])
@ApplicationScope
interface ApplicationComponent {

    fun inject(fragment: MapFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appication: Application): ApplicationComponent
    }
}
