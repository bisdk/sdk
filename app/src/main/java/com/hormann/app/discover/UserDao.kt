package com.hormann.app.discover

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM gateway")
    fun getAll(): LiveData<List<Gateway>>

    @Query("SELECT * FROM gateway WHERE host like :search")
    fun search(search: String): List<Gateway>

    @Query("SELECT * FROM gateway WHERE uid IN (:gatewayIds)")
    fun loadAllByIds(gatewayIds: IntArray): List<Gateway>

    @Insert
    fun insertAll(vararg gateways: Gateway)

    @Delete
    fun delete(gateway: Gateway)

    @Query("DELETE FROM gateway")
    fun clear()
}