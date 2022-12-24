package protrnd.com.data.network.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import protrnd.com.data.models.Profile

@Dao
interface ProfileDao {
    @Query("SELECT * FROM Profiles WHERE id = :id")
    fun getProfile(id: String): Flow<Profile>

    @Query("SELECT COUNT(*) FROM Profiles")
    fun getDBSize(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Query("SELECT * FROM Profiles")
    fun getProfiles(): Flow<List<Profile>>

    @Query("DELETE FROM Profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)
}