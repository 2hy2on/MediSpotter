package ddwucom.mobile.medispotter.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Hospital::class], version = 1)
abstract class HospitalDatabase: RoomDatabase() {
    abstract fun hospitalDao(): HospitalDao
}