package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.user.UserDao
import com.example.smartpaws.data.local.user.UserEntity

class UserRepository(
    private val userDao: UserDao
){
    //login
    suspend fun login(email:String, pass: String): Result<UserEntity>{
        val user = userDao.getByEmail(email)
        return if(user != null && user.password == pass){
            Result.success(user)
        }
        else{
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    //register
    suspend fun register(name:String, email: String, phone: String, pass: String): Result<Long>{
        val exists = userDao.getByEmail(email) != null
        if(exists){
            return Result.failure(IllegalArgumentException("Correo ya existente"))
        }
        else{
            val id = userDao.insert(
                UserEntity(
                    name = name,
                    email =  email,
                    phone = phone,
                    password = pass
                )
            )
            return Result.success(id)
        }
    }
    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }
}