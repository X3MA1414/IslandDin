package com.chema.isla

import android.app.Application
import com.chema.isla.di.AppContainer

class IslaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val android.content.Context.appContainer: AppContainer
    get() = (applicationContext as IslaApp).container
