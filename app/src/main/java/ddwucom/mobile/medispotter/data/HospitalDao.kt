package ddwucom.mobile.medispotter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HospitalDao {

    @Query("SELECT * FROM hospital_table")
    fun getAllHospitals(): List<Hospital>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hospitals: List<Hospital>)
    @Query("SELECT * FROM hospital_table WHERE dutyAddr LIKE '%' || :addr || '%'")
    suspend fun getHospitalByAddr(addr: String): List<Hospital>

    @Query("SELECT * FROM hospital_table WHERE dutyAddr LIKE '%' || :addr || '%' AND dutyDivName = :type")
    suspend fun getHospitalByAddrType(addr: String, type:String): List<Hospital>
    @Query("SELECT * FROM hospital_table WHERE name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalByNameType(name: String, type:String): List<Hospital>

    @Query("SELECT * FROM hospital_table where _id = :hospitalId")
    suspend fun getHospitalById(hospitalId: Int): Hospital

    @Query("UPDATE hospital_table SET rating = :newRate, review = :newReview WHERE _id = :hospitalId")
    suspend fun updateHospitalRateAndReview(hospitalId: Int?, newRate: Float, newReview: String)

    @Query("UPDATE hospital_table SET rating = null, review = null WHERE _id = :hospitalId")
    suspend fun deleteHospitalRateAndReview(hospitalId: Int?)

    @Query("SELECT * FROM hospital_table WHERE review IS NOT NULL")
    suspend fun getHospitalReview(): List<Hospital>

    @Query("UPDATE hospital_table SET isFavorite = :favorite WHERE _id = :hospitalId")
    suspend fun updateFavorite(hospitalId: Int, favorite: Boolean)

    @Query("SELECT * FROM hospital_table WHERE isFavorite = 1")
    suspend fun getHospitalFavorite(): List<Hospital>

}
