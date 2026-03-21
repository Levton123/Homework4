package com.example.pokedex.data.repository

import com.example.pokedex.data.db.FavouriteDao
import com.example.pokedex.data.db.FavouriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FavouriteRepository @Inject constructor(
    private val dao: FavouriteDao
) {
    open val favouriteIds: Flow<List<Int>> = dao.getFavouriteIds()

    open suspend fun addFavourite(pokemonId: Int, pokemonName: String) {
        dao.addFavourite(FavouriteEntity(pokemonId = pokemonId, pokemonName = pokemonName))
    }

    open suspend fun removeFavourite(pokemonId: Int) {
        dao.removeFavourite(pokemonId)
    }

    open suspend fun isFavourite(pokemonId: Int): Boolean = dao.isFavourite(pokemonId)
}