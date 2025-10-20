package com.example.smartpaws.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartpaws.data.local.user.UserDao
import com.example.smartpaws.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase(){
    //integramos el DAO de cada entidad
    abstract fun userDao(): UserDao

    companion object{

        //variable para instanciar la BD
        @Volatile
        private var INSTANCE: com.example.smartpaws.data.local.database.AppDatabase? = null
        //variable para el nombre
        private const val DB_NAME = "ui_navegacion.db"

        //obtener la instancia unica de BD
        fun getInstance(context: Context): com.example.smartpaws.data.local.database.AppDatabase {
            //construimos la BD
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    com.example.smartpaws.data.local.database.AppDatabase::class.java,
                    DB_NAME
                )
                    //definir una funcion que se ejecuta unicamente cuando es la 1era vez
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            //lanzamos una corrutina para ejecutar los insert
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).userDao()
                                //creamos las semillas de los insert
                                val seed = listOf(
                                    UserEntity(
                                        id = 0L,
                                        name = "Admin",
                                        email = "a@a.cl",
                                        phone = "12345678",
                                        password = "Admin123!"
                                    ),
                                    UserEntity(
                                        id = 0L,
                                        name = "Jose",
                                        email = "b@b.cl",
                                        phone = "12345678",
                                        password = "Jose123!"
                                    )

                                )
                                //INSERTAR SI NO HAY REGISTRO EN LA TABLA
                                if (dao.count() == 0) {
                                    seed.forEach { dao.insert(it) }
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