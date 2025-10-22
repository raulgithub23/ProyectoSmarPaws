package com.example.smartpaws.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartpaws.data.local.appointment.AppointmentDao
import com.example.smartpaws.data.local.appointment.AppointmentEntity
import com.example.smartpaws.data.local.doctors.DoctorDao
import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.doctors.DoctorScheduleEntity
import com.example.smartpaws.data.local.pets.PetFactDao
import com.example.smartpaws.data.local.pets.PetFactEntity
import com.example.smartpaws.data.local.pets.PetsDao
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.data.local.user.UserDao
import com.example.smartpaws.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PetsEntity::class,
        DoctorEntity::class,
        DoctorScheduleEntity::class,
        AppointmentEntity::class,
        PetFactEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petsDao(): PetsDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun petFactDao(): PetFactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val userDao = getInstance(context).userDao()
                                val petsDao = getInstance(context).petsDao()
                                val doctorDao = getInstance(context).doctorDao()
                                val appointmentDao = getInstance(context).appointmentDao()
                                val petFactDao = getInstance(context).petFactDao()

                                val userSeed = listOf(
                                    UserEntity(
                                        name = "Admin",
                                        email = "a@a.cl",
                                        phone = "12345678",
                                        password = "Admin123!"
                                    ),
                                    UserEntity(
                                        name = "Jose",
                                        email = "b@b.cl",
                                        phone = "12345678",
                                        password = "Jose123!"
                                    )
                                )
                                if (userDao.count() == 0) {
                                    userSeed.forEach { userDao.insert(it) }
                                }

                                val petsSeed = listOf(
                                    PetsEntity(
                                        userId = 1,
                                        name = "Firulais",
                                        especie = "Perro",
                                        fechaNacimiento = "2020-05-15",
                                        peso = 12.5f,
                                        genero = "M",
                                        color = "Café",
                                        notas = "Le gusta jugar con pelotas"
                                    ),
                                    PetsEntity(
                                        userId = 1,
                                        name = "Michi",
                                        especie = "Gato",
                                        fechaNacimiento = "2021-08-20",
                                        peso = 4.2f,
                                        genero = "F",
                                        color = "Gris",
                                        notas = "Muy dormilona"
                                    ),
                                    PetsEntity(
                                        userId = 2,
                                        name = "Rex",
                                        especie = "Perro",
                                        fechaNacimiento = "2019-03-10",
                                        peso = 25.0f,
                                        genero = "M",
                                        color = "Negro",
                                        notas = "Muy protector"
                                    )
                                )
                                if (petsDao.count() == 0) {
                                    petsSeed.forEach { petsDao.insert(it) }
                                }

                                //  Doctores y sus horarios
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
