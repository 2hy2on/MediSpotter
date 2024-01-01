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

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN :startColumnName AND :endColumnName AND dutyAddr LIKE '%' || :addr || '%' AND dutyDivName = :type")
    suspend fun getHospitalOpen(addr: String, type: String, current: String, startColumnName: String, endColumnName: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime1s AND dutyTime1c AND name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalNameMonOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime2s AND dutyTime2c AND name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalNameTueOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime3s AND dutyTime3c AND name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalNameWedOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime4s AND dutyTime4c AND name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalNameThurOpen(name: String, type: String, current: String): List<Hospital>
    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime5s AND dutyTime5c AND name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalNameFriOpen(name: String, type: String, current: String): List<Hospital>
    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime6s AND dutyTime6c AND name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalNameSatOpen(name: String, type: String, current: String): List<Hospital>
    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime7s AND dutyTime7c AND name LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalNameSunOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime1s AND dutyTime1c AND dutyAddr LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalAddrMonOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime2s AND dutyTime2c AND dutyAddr LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalAddrTueOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime3s AND dutyTime3c AND dutyAddr LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalAddrWedOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime4s AND dutyTime4c AND dutyAddr LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalAddrThurOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime5s AND dutyTime5c AND dutyAddr LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalAddrFriOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime6s AND dutyTime6c AND dutyAddr LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalAddrSatOpen(name: String, type: String, current: String): List<Hospital>

    @Query("SELECT * FROM HOSPITAL_TABLE WHERE :current BETWEEN dutyTime7s AND dutyTime7c AND dutyAddr LIKE '%' || :name || '%' AND dutyDivName = :type")
    suspend fun getHospitalAddrSunOpen(name: String, type: String, current: String): List<Hospital>
}
