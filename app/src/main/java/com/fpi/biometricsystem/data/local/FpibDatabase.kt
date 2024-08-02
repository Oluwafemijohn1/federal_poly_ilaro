package com.fpi.biometricsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.fpi.biometricsystem.data.Event
import com.fpi.biometricsystem.data.local.models.StaffInfo
import com.fpi.biometricsystem.data.local.models.StudentInfo
import com.fpi.biometricsystem.utils.toObject
import com.google.gson.Gson

@Database(entities = [StaffInfo::class, Event::class, StudentInfo::class], version = 1, exportSchema = false)
@TypeConverters(
    EventConverter::class,
)
abstract class FpibDatabase : RoomDatabase() {

    abstract fun staffDao(): StaffDao
    abstract fun studentDao(): StudentDao
    abstract fun eventDao(): EventDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: FpibDatabase? = null

        fun getDatabase(context: Context): FpibDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FpibDatabase::class.java,
                    "fpib_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

class EventConverter {
    @TypeConverter
    fun staffListToString(data: List<StaffInfo>): String {
        return Gson().toJson(data)
    }

    @TypeConverter
    fun stringToStaffList(data: String): List<StaffInfo> {
        return data.toObject()
    }


    @TypeConverter
    fun studentListToString(data: List<StudentInfo>): String {
        return Gson().toJson(data)
    }

    @TypeConverter
    fun stringToStudentList(data: String): List<StudentInfo> {
        return data.toObject()
    }

    @TypeConverter
    fun listToString(data: List<String>): String {
        return Gson().toJson(data)
    }

    @TypeConverter
    fun stringToList(data: String): List<String> {
        return data.toObject()
    }
}