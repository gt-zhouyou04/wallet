package com.example.wallet.CustomManager

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    @ColumnInfo(name = "name") val name: String
)

@Dao
interface TagDao {
    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: Tag)

    @Query("SELECT * FROM tags")
    fun getAllTags(): LiveData<List<Tag>>

    @Query("SELECT * FROM tags")
    fun getAllTagsCurrent(): List<Tag>

    @Transaction
    suspend fun deleteAllAndResetIds() {
        deleteAll()  // 清空表中的数据
        resetPrimaryKey() // 重置主键
    }

    @Query("DELETE FROM sqlite_sequence WHERE name = 'tags'")
    suspend fun resetPrimaryKey()
}