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
import com.example.smartpaws.data.local.pets.PetFactEntity
import com.example.smartpaws.data.local.pets.PetFactDao
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
    version = 8,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase(){
    //integramos el DAO de cada entidad
    abstract fun userDao(): UserDao
    abstract fun petsDao(): PetsDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun petFactDao(): PetFactDao


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
                                val appointmentDao = getInstance(context).appointmentDao()
                                val petFactDao = getInstance(context).petFactDao()

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
                                        userId = 2, name = "Rex", especie = "Perro", fechaNacimiento = "2019-03-10", peso = 25.0f, genero = "M", color = "Negro", notas = "Muy protector"
                                    )
                                )

                                //INSERTAR SI NO HAY REGISTRO EN LA TABLA
                                if (petsDao.count() == 0) {
                                    petsSeed.forEach { petsDao.insert(it) }
                                }

                                // ========== Doctores mejorados con más especialidades ==========
                                //  Doctores y sus horarios


                                val doctorsSeed = listOf(
                                    DoctorEntity(
                                        name = "Dr. Carlos Méndez",
                                        specialty = "Veterinario General",
                                        email = "carlos.mendez@smartpaws.cl",
                                        phone = "+56912345678"
                                    ),
                                    DoctorEntity(
                                        name = "Dra. María González",
                                        specialty = "Cirugía Veterinaria",
                                        email = "maria.gonzalez@smartpaws.cl",
                                        phone = "+56987654321"
                                    ),
                                    DoctorEntity(
                                        name = "Dr. Jorge Silva",
                                        specialty = "Animales Exóticos",
                                        email = "jorge.silva@smartpaws.cl",
                                        phone = "+56911223344"
                                    ),
                                    DoctorEntity(
                                        name = "Dra. Ana Rojas",
                                        specialty = "Dermatología Veterinaria",
                                        email = "ana.rojas@smartpaws.cl",
                                        phone = "+56922334455"
                                    ),
                                    DoctorEntity(
                                        name = "Dr. Luis Pérez",
                                        specialty = "Odontología Veterinaria",
                                        email = "luis.perez@smartpaws.cl",
                                        phone = "+56933445566"
                                    )
                                )

                                //INSERTAR SI NO HAY REGISTRO EN LA TABLA
                                if (doctorDao.count() == 0) {
                                    doctorsSeed.forEachIndexed { index, doctor ->
                                        val doctorId = doctorDao.insert(doctor)

                                        // Horarios específicos para cada doctor según su especialidad
                                        val schedules = when(index) {
                                            0 -> listOf( // Dr. Carlos Méndez - Veterinario General (Lunes a Viernes completo)
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Lunes", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Martes", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Miércoles", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Jueves", startTime = "09:00", endTime = "18:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Viernes", startTime = "09:00", endTime = "17:00")
                                            )
                                            1 -> listOf( // Dra. María González - Cirugía (Lunes, Miércoles, Viernes)
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Lunes", startTime = "10:00", endTime = "16:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Miércoles", startTime = "10:00", endTime = "16:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Viernes", startTime = "10:00", endTime = "16:00")
                                            )
                                            2 -> listOf( // Dr. Jorge Silva - Exóticos (Martes, Jueves, Sábado)
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Martes", startTime = "11:00", endTime = "19:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Jueves", startTime = "11:00", endTime = "19:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Sábado", startTime = "09:00", endTime = "14:00")
                                            )
                                            3 -> listOf( // Dra. Ana Rojas - Dermatología (Mañanas Lunes a Viernes)
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Lunes", startTime = "08:00", endTime = "14:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Martes", startTime = "08:00", endTime = "14:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Miércoles", startTime = "08:00", endTime = "14:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Jueves", startTime = "08:00", endTime = "14:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Viernes", startTime = "08:00", endTime = "14:00")
                                            )
                                            4 -> listOf( // Dr. Luis Pérez - Odontología (Tardes Miércoles a Sábado)
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Miércoles", startTime = "14:00", endTime = "20:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Jueves", startTime = "14:00", endTime = "20:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Viernes", startTime = "14:00", endTime = "20:00"),
                                                DoctorScheduleEntity(doctorId = doctorId, dayOfWeek = "Sábado", startTime = "10:00", endTime = "15:00")
                                            )
                                            else -> emptyList()
                                        }
                                        //insertar los horarios de cada doctor
                                        doctorDao.insertSchedules(schedules)
                                    }
                                }

                                // --- Citas seed (ejemplos iniciales) ---
                                if (appointmentDao.count() == 0) {
                                    val citaSeed = listOf(
                                        AppointmentEntity(
                                            userId = 1,
                                            petId = 1, // Firulais
                                            doctorId = 1, // Dr. Carlos Méndez
                                            date = "2025-10-22",
                                            time = "10:30",
                                            notes = "Vacunación anual"
                                        ),
                                        AppointmentEntity(
                                            userId = 1,
                                            petId = 2, // Michi
                                            doctorId = 4, // Dra. Ana Rojas (Dermatología)
                                            date = "2025-10-23",
                                            time = "09:00",
                                            notes = "Revisión de piel por alergia"
                                        ),
                                        AppointmentEntity(
                                            userId = 2,
                                            petId = 3, // Rex
                                            doctorId = 2, // Dra. María González (Cirugía)
                                            date = "2025-10-25",
                                            time = "11:00",
                                            notes = "Control post-operatorio"
                                        ),
                                        AppointmentEntity(
                                            userId = 1,
                                            petId = 1, // Firulais
                                            doctorId = 5, // Dr. Luis Pérez (Odontología)
                                            date = "2025-10-26",
                                            time = "15:30",
                                            notes = "Limpieza dental"
                                        )
                                    )
                                    //insertar las citas de ejemplo
                                    citaSeed.forEach { appointmentDao.insert(it) }
                                }

                                // --- PetFacts seed (Datos curiosos) ---
                                if (petFactDao.count() == 0) {
                                    val petFactsSeed = listOf(
                                        // Datos sobre GATOS
                                        PetFactEntity(
                                            type = "cat",
                                            title = "Datos sobre gatos",
                                            fact = "Los gatos duermen entre 13 y 16 horas al día, ¡más de la mitad de su vida!"
                                        ),
                                        PetFactEntity(
                                            type = "cat",
                                            title = "Datos sobre gatos",
                                            fact = "Los gatos tienen 32 músculos en cada oreja, permitiéndoles rotarlas 180 grados."
                                        ),
                                        PetFactEntity(
                                            type = "cat",
                                            title = "Datos sobre gatos",
                                            fact = "El ronroneo de un gato puede ayudar a sanar huesos y reducir el estrés."
                                        ),
                                        PetFactEntity(
                                            type = "cat",
                                            title = "Datos sobre gatos",
                                            fact = "Los gatos pueden hacer más de 100 sonidos vocales diferentes."
                                        ),
                                        PetFactEntity(
                                            type = "cat",
                                            title = "Datos sobre gatos",
                                            fact = "Un gato puede saltar hasta 6 veces su longitud en un solo salto."
                                        ),

                                        // Datos sobre PERROS
                                        PetFactEntity(
                                            type = "dog",
                                            title = "Datos sobre perros",
                                            fact = "Los perros tienen un sentido del olfato 10,000 veces más fuerte que los humanos."
                                        ),
                                        PetFactEntity(
                                            type = "dog",
                                            title = "Datos sobre perros",
                                            fact = "La nariz de cada perro es única, como las huellas dactilares humanas."
                                        ),
                                        PetFactEntity(
                                            type = "dog",
                                            title = "Datos sobre perros",
                                            fact = "Los perros pueden entender hasta 250 palabras y gestos diferentes."
                                        ),
                                        PetFactEntity(
                                            type = "dog",
                                            title = "Datos sobre perros",
                                            fact = "Los cachorros nacen sordos, ciegos y sin dientes."
                                        ),
                                        PetFactEntity(
                                            type = "dog",
                                            title = "Datos sobre perros",
                                            fact = "Los perros sudan a través de sus patas, no por jadear."
                                        )
                                    )
                                    petFactsSeed.forEach { petFactDao.insertFact(it) }
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