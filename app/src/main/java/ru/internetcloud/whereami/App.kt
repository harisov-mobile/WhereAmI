package ru.internetcloud.whereami

import android.app.Application
import ru.internetcloud.whereami.di.ApplicationComponent
import ru.internetcloud.whereami.di.DaggerApplicationComponent

class App: Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

}
