package com.example.pokedex.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {

    /** Возвращает Flow со всеми id избранных — UI автоматически обновится при любом изменении. */
    @Query("SELECT pokemonId FROM favourites ORDER BY addedAt DESC")
    fun getFavouriteIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE pokemonId = :pokemonId")
    suspend fun removeFavourite(pokemonId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE pokemonId = :pokemonId)")
    suspend fun isFavourite(pokemonId: Int): Boolean
}