package com.fpi.biometricsystem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fpi.biometricsystem.data.Event
import dagger.Provides
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events_table ORDER BY title ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events_table WHERE id = :eventId LIMIT 1")
    fun getSingleEvent(eventId: String): Event

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: Event)

    @Query("DELETE FROM events_table")
    suspend fun deleteAll()
}