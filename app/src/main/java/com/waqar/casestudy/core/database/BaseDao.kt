package com.waqar.casestudy.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface BaseDao<T> {

    /**
     * Insert a list in the database. If the task already exists, replace it.
     *
     * @param list the items to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertAll(list: List<T>)

    /**
     * Insert item in the database. If the task already exists, replace it.
     *
     * @param entity the item to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: T)

    /**
     * Update item in the database.
     *
     * @param entity the item to be updated.
     */
    @Update
    fun update(entity: T)
}