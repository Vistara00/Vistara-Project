package com.example.vistaraapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room


@Database(
    entities = [Contact::class],
    version = 4
)
abstract class ContactDatabase : RoomDatabase() {

    abstract val dao: ContactDao

    companion object {
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        fun getDatabase(context: Context): ContactDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "contacts.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}