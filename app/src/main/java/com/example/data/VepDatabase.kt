package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PipelineRunEntity::class,
        LayerStatusEntity::class,
        SelfHealingPatchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VepDatabase : RoomDatabase() {
    abstract fun vepDao(): VepDao

    companion object {
        @Volatile
        private var INSTANCE: VepDatabase? = null

        fun getDatabase(context: Context): VepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VepDatabase::class.java,
                    "vep_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
