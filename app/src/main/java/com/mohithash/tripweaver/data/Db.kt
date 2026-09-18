package com.mohithash.tripweaver.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destination: String,
    val startDate: String,
    val days: Int,
    /** domain.TripBrief JSON */
    val brief: String,
    /** domain.Itinerary JSON, empty until generated */
    val itinerary: String = "",
    /** Comma-separated packed item indices */
    val packed: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startDate DESC") fun all(): Flow<List<Trip>>
    @Insert suspend fun insert(t: Trip): Long
    @Update suspend fun update(t: Trip)
    @Query("DELETE FROM trips WHERE id = :id") suspend fun delete(id: Long)
}

@Database(entities = [Trip::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() { abstract fun trips(): TripDao }
