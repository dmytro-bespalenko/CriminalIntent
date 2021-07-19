package com.example.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminalintent.database.CrimeDatabase
import com.example.criminalintent.database.migration_1_2
import java.io.File
import java.util.*

private const val DATABASE_NAME = "crime-database"

class CrimeRepository(context: Context) {


    private val filesDir = context.applicationContext.filesDir


    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)
        .build()


    private val crimeDao = database.crimeDao()

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    suspend fun updateCrime(crime: Crime) {

        crimeDao.updateCrime(crime)
    }

    suspend fun addCrime(crime: Crime) {
        crimeDao.insertCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)


    suspend fun deleteCrime(crimeId: UUID) {
        crimeDao.removeCrime(crimeId)
    }

    companion object {

        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository =
            INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")


    }


}