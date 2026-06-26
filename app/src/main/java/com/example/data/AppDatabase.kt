package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val fileSize: Long,
    val isSent: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers")
    suspend fun clearHistory()
}

@Database(entities = [TransferEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transferDao(): TransferDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quickshare_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class TransferRepository(private val transferDao: TransferDao) {
    val allTransfers: Flow<List<TransferEntity>> = transferDao.getAllTransfers()

    suspend fun insert(transfer: TransferEntity) = transferDao.insertTransfer(transfer)

    suspend fun clearHistory() = transferDao.clearHistory()
}
