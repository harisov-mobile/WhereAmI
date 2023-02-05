package ru.internetcloud.wereami

import android.app.Application
import ru.internetcloud.wereami.di.ApplicationComponent
import ru.internetcloud.wereami.di.DaggerApplicationComponent

class App: Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

}
