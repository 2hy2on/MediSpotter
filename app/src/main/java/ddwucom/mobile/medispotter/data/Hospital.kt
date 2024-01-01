package ddwucom.mobile.medispotter.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "hospital_table")
data class Hospital(
    @PrimaryKey(autoGenerate = true)
    val _id: Int?,

    var dutyAddr: String?,
    var dutyDivName: String?,
    var latitude: String?,
    var longitude: String?,
    var name: String?,
    var dutyInfo: String?,
    var dutyTel1: String?,
    var dutyTime1c: String?,
    var dutyTime1s: String?,
    var dutyTime2c: String?,
    var dutyTime2s: String?,
    var dutyTime3c: String?,
    var dutyTime3s: String?,
    var dutyTime4c: String?,
    var dutyTime4s: String?,
    var dutyTime5c: String?,
    var dutyTime5s: String?,
    var dutyTime6c: String?,
    var dutyTime6s: String?,
    var dutyTime7c: String?,
    var dutyTime7s: String?,
    var dutyTime8c: String?,
    var dutyTime8s: String?,
    var rating: Float?,
    var review: String?,
    var isFavorite: Int
): Serializable