package com.example.pokedex.data.repository

import com.example.pokedex.data.db.FavouriteDao
import com.example.pokedex.data.db.FavouriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий избранного.
 * Единственный источник правды для избранных покемонов — Room.
 * Flow из DAO автоматически уведомляет подписчиков при любом изменении в БД.
 */
@Singleton
class FavouriteRepository @Inject constructor(
    private val dao: FavouriteDao
) {
    /** Flow с актуальным списком id избранных. Пересоздавать не нужно — живёт всё время. */
    val favouriteIds: Flow<List<Int>> = dao.getFavouriteIds()

    suspend fun toggleFavourite(pokemonId: Int, pokemonName: String) {
        if (dao.isFavourite(pokemonId)) {
            dao.removeFavourite(pokemonId)
        } else {
            dao.addFavourite(FavouriteEntity(pokemonId = pokemonId, pokemonName = pokemonName))
        }
    }
}