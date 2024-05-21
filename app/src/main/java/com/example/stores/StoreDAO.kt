package com.example.stores

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface StoreDao {
    @Query("SELECT  * FROM StoreEntity")
    fun getAllStores(): MutableList<StoreEntity>
    @Query("SELECT  * FROM StoreEntity where id = :id")
    fun getStoreById(id:Long): StoreEntity
    @Insert
    fun addStore( storeEntity: StoreEntity): Long
    @Update
    fun updateStore( storeEntity: StoreEntity)
    @Delete
    fun deleteStore(storeAdapter: StoreEntity)

}