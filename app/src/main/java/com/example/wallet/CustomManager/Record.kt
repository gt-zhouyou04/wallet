package com.example.wallet.CustomManager

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "tag_id") val tagId: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Record)

    @Query("SELECT * FROM records WHERE timestamp >= :from AND timestamp <= :to")
    fun getRecordsByTime(from: Long, to: Long): LiveData<List<Record>>

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM records")
    fun getAllRecords(): LiveData<List<Record>>
}
