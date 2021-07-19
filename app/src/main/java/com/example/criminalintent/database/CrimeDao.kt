package com.example.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.criminalintent.Crime
import retrofit2.http.DELETE
import java.util.*

@Dao
interface CrimeDao {

    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update()
    suspend fun updateCrime(crime: Crime)

    @Insert
    suspend fun insertCrime(crime: Crime)

    @Query("DELETE FROM crime WHERE id = :id")
    suspend fun removeCrime(id: UUID)
}