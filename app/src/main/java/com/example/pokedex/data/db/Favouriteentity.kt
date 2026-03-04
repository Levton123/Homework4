package com.example.pokedex.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность для хранения избранных покемонов.
 * Сценарий: Favourites — избранное переживает перезапуск приложения.
 */
@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey
    val pokemonId: Int,
    val pokemonName: String,
    val addedAt: Long = System.currentTimeMillis()
)