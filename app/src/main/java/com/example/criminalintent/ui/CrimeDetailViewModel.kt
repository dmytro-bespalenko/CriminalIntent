package com.example.criminalintent.ui

import androidx.lifecycle.*
import com.example.criminalintent.Crime
import com.example.criminalintent.CrimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class CrimeDetailViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    fun saveCrime(crime: Crime) {
        viewModelScope.launch(Dispatchers.IO) {
            crimeRepository.updateCrime(crime)
        }
    }


    fun addCrime(crime: Crime) {
        viewModelScope.launch(Dispatchers.IO) {
            crimeRepository.addCrime(crime)
        }
    }

    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }

    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun getPhotoFile(crime: Crime): File {

        return crimeRepository.getPhotoFile(crime)
    }


}