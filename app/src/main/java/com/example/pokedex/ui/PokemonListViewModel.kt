package com.example.pokedex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.repository.FavouriteRepository
import com.example.pokedex.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)

    val favourites: StateFlow<Set<Int>> = favouriteRepository.favouriteIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val uiState: StateFlow<PokemonListUiState> = combine(_listState, favourites) { listState, favs ->
        when (listState) {
            is PokemonListUiState.Success -> listState.copy(favourites = favs)
            else -> listState
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PokemonListUiState.Loading)

    private var searchJob: Job? = null

    init {
        loadPokemonList()
    }

    fun onEvent(event: PokemonListEvent) {
        when (event) {
            is PokemonListEvent.Search -> performSearch(event.query)
            is PokemonListEvent.Retry -> loadPokemonList()
            is PokemonListEvent.Refresh -> { searchJob?.cancel(); loadPokemonList() }
            is PokemonListEvent.AddFavourite -> addFavourite(event.pokemonId, event.pokemonName)
            is PokemonListEvent.RemoveFavourite -> removeFavourite(event.pokemonId)
        }
    }

    private fun loadPokemonList() {
        viewModelScope.launch {
            _listState.value = PokemonListUiState.Loading
            pokemonRepository.getPokemonList().fold(
                onSuccess = { list ->
                    _listState.value = if (list.isEmpty()) PokemonListUiState.Empty
                    else PokemonListUiState.Success(pokemonList = list)
                },
                onFailure = { error ->
                    _listState.value = PokemonListUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            pokemonRepository.searchPokemon(query).fold(
                onSuccess = { results ->
                    _listState.value = if (results.isEmpty()) PokemonListUiState.Empty
                    else PokemonListUiState.Success(pokemonList = results, searchQuery = query)
                },
                onFailure = { error ->
                    _listState.value = PokemonListUiState.Error(error.message ?: "Search failed")
                }
            )
        }
    }

    private fun addFavourite(pokemonId: Int, pokemonName: String) {
        viewModelScope.launch { favouriteRepository.addFavourite(pokemonId, pokemonName) }
    }

    private fun removeFavourite(pokemonId: Int) {
        viewModelScope.launch { favouriteRepository.removeFavourite(pokemonId) }
    }
}

sealed interface PokemonListEvent {
    data class Search(val query: String) : PokemonListEvent
    data object Retry : PokemonListEvent
    data object Refresh : PokemonListEvent
    data class AddFavourite(val pokemonId: Int, val pokemonName: String) : PokemonListEvent
    data class RemoveFavourite(val pokemonId: Int) : PokemonListEvent
}