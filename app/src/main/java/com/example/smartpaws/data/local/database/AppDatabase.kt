package com.example.smartpaws.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartpaws.data.local.doctors.DoctorDao
import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.doctors.DoctorScheduleEntity
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
        DoctorScheduleEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase(){
    //integramos el DAO de cada entidad
    abstract fun userDao(): UserDao
    abstract fun petsDao(): PetsDao
    abstract fun doctorDao(): DoctorDao

    companion object{

        //variable para instanciar la BD
        @Volatile
        private var INSTANCE: AppDatabase? = null
        //variable para el nombre
        private const val DB_NAME = "ui_navegacion.db"

        //obtener la instancia unica de BD
        fun getInstance(context: Context): AppDatabase {
            //construimos la BD
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    //definir una funcion que se ejecuta unicamente cuando es la 1era vez
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            //lanzamos una corrutina para ejecutar los insert
                            CoroutineScope(Dispatchers.IO).launch {
                                val userDao = getInstance(context).userDao()
                                val petsDao = getInstance(context).petsDao()
                                val doctorDao = getInstance(context).doctorDao()

                                //creamos las semillas de los insert de usuarios
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
                                //INSERTAR SI NO HAY REGISTRO EN LA TABLA
                                if (userDao.count() == 0) {
                                    userSeed.forEach { userDao.insert(it) }
                                }

                                //creamos las semillas de los insert de mascotas
                                val petsSeed = listOf(
                                    PetsEntity(
                                        userId = 1, name = "Firulais", especie = "Perro", fechaNacimiento = "2020-05-15", peso = 12.5f, genero = "M", color = "Café", notas = "Le gusta jugar con pelotas"
                                    ),
                                    PetsEntity(
                                        userId = 1, name = "Michi", especie = "Gato", fechaNacimiento = "2021-08-20", peso = 4.2f, genero = "F", color = "Gris", notas = "Muy dormilona"
                                    ),
                                    PetsEntity(
                                        userId = 2,  name = "Rex", especie = "Perro", fechaNacimiento = "2019-03-10", peso = 25.0f, genero = "M", color = "Negro", notas = "Muy protector"
                                    )
                                )

                                //INSERTAR SI NO HAY REGISTRO EN LA TABLA
                                if (petsDao.count() == 0) {
                                    petsSeed.forEach { petsDao.insert(it) }
                                }

                                // 3️⃣ TERCERO: Doctores y sus horarios
                                val doctorsSeed = listOf(
                                    DoctorEntity(
                                        name = "Dra. María González",
                                        specialty = "Veterinario General",
                                        phone = "987654321",
                                        email = "maria@smartpaws.cl"
                                    ),
                                    DoctorEntity(
                                        name = "Dr. Carlos Ruiz",
                                        specialty = "Cirujano Veterinario",
                                        phone = "987654322",
                                        email = "carlos@smartpaws.cl"
                                    )
                                )

                                if (doctorDao.count() == 0) {
                                    doctorsSeed.forEachIndexed { index, doctor ->
                                        val doctorId = doctorDao.insert(doctor)  //

                                        // Horarios para cada doctor
                                        val schedules = when(index) {
                                            0 -> listOf( // Dra. María (Lunes a Viernes)
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Lunes", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Martes", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Miércoles", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Jueves", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Viernes", startTime = "09:00", endTime = "18:00")
                                            )
                                            1 -> listOf( // Dr. Carlos (Martes, Jueves, Sábado)
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Martes", startTime = "10:00", endTime = "14:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Jueves", startTime = "10:00", endTime = "14:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Sábado", startTime = "09:00", endTime = "13:00")
                                            )
                                            else -> emptyList()
                                        }
                                        doctorDao.insertSchedules(schedules)
                                    }
                                }
                            }
                        }
                    })
                    //destruyo todos los elementos anteriores
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}