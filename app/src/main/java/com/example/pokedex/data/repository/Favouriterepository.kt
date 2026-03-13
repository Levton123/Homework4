package com.example.pokedex.data.repository

import com.example.pokedex.data.db.FavouriteDao
import com.example.pokedex.data.db.FavouriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteRepository @Inject constructor(
    private val dao: FavouriteDao
) {
    val favouriteIds: Flow<List<Int>> = dao.getFavouriteIds()

    suspend fun addFavourite(pokemonId: Int, pokemonName: String) {
        dao.addFavourite(FavouriteEntity(pokemonId = pokemonId, pokemonName = pokemonName))
    }

    suspend fun removeFavourite(pokemonId: Int) {
        dao.removeFavourite(pokemonId)
    }

    suspend fun isFavourite(pokemonId: Int): Boolean = dao.isFavourite(pokemonId)
}