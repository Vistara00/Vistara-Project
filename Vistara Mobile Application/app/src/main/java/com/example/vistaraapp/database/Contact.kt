package com.example.vistaraapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
//this creates a table  in a database
@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int?=0,
    val fullName: String,
    val email: String,
    val phoneNumber:String,
    val idNumber: String,
    val emergencyNumber: String,
    val isCurrentUser: Boolean = false

    )