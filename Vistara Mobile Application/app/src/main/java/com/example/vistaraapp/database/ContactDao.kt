package com.example.vistaraapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
//This is the brain  that talks to  my database
@Dao
interface ContactDao {

    @Upsert
    suspend fun upsertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contact ORDER BY fullName ASC")
    fun getContactsOrderedByFullName(): Flow<List<Contact>>

    @Query("SELECT * FROM contact ORDER BY email ASC")
    fun getContactsOrderedByEmail(): Flow<List<Contact>>

    @Query("SELECT * FROM contact ORDER BY phoneNumber ASC")
    fun getContactsOrderedByPhoneNumber(): Flow<List<Contact>>

    @Query("SELECT * FROM contact ORDER BY idNumber ASC")
    fun getContactsOrderedByIdNumber(): Flow<List<Contact>>

    @Query("SELECT * FROM contact ORDER BY emergencyNumber ASC")
    fun getContactsOrderedByEmergencyNumber(): Flow<List<Contact>>

    //Method for  Offline Login
    @Query("SELECT * FROM contact WHERE email = :email LIMIT 1")
    suspend fun getContactByEmail(email: String): Contact?

    // Reactive stream for the single user profile row (always id = 1)
    @Query("SELECT * FROM contact WHERE isCurrentUser = 1 LIMIT 1")
    fun getUserProfile(): Flow<Contact?>

    @Query("SELECT * FROM contact WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Int): Contact?
}