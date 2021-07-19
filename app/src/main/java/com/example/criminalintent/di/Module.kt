package com.example.criminalintent.di

import com.example.criminalintent.Crime
import com.example.criminalintent.ui.CrimeDetailViewModel
import com.example.criminalintent.ui.CrimeListViewModel
import com.example.criminalintent.CrimeRepository
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { CrimeRepository(get()) }
    factory { Crime(get(), get(), get(), get()) }
    viewModel { CrimeListViewModel() }
    viewModel { CrimeDetailViewModel() }

}