package com.example.criminalintent

import android.app.Application
import com.example.criminalintent.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CriminalIntentApplication: Application() {


    override fun onCreate() {
        super.onCreate()

        CrimeRepository.initialize(this)

        startKoin {
            androidContext(this@CriminalIntentApplication)

            modules(listOf(appModule))
        }

    }
}