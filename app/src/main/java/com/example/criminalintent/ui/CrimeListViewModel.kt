package com.example.criminalintent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criminalintent.Crime
import com.example.criminalintent.CrimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class CrimeListViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()

    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        viewModelScope.launch(Dispatchers.IO) {
            crimeRepository.addCrime(crime)
        }

    }

    fun removeCrime(crimeId: UUID) {
        viewModelScope.launch(Dispatchers.IO) {
            crimeRepository.deleteCrime(crimeId)
        }
    }


}