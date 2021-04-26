package com.waqar.casestudy.core.database

import android.content.Context
import androidx.room.*
import com.waqar.casestudy.constants.DatabaseConstants
import com.waqar.casestudy.core.model.ExerciseModel

// adding annotation for our database entities and db version.
@Database(
    entities = [ExerciseModel::class],
    version = DatabaseConstants.DATABASE_VERSION,
    exportSchema = false
)
abstract class KaiaDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: KaiaDatabase? = null

        fun getDatabase(context: Context): KaiaDatabase {
            return instance ?: synchronized(this) {
                // create database here
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaiaDatabase::class.java,
                    DatabaseConstants.DATABASE_NAME
                )
                    .allowMainThreadQueries() // allows Room to executing task in main thread
                    .fallbackToDestructiveMigration() // allows Room to recreate database if no migrations found
                    .build()

                this.instance = instance
                instance
            }
        }
    }

    abstract fun getExerciseDao(): ExerciseDao
}