package com.example.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.repository.FavouriteRepository
import com.example.pokedex.data.repository.PokemonRepository
import com.example.pokedex.ui.state.PokemonListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()

    // Flow избранных из Room — единственный источник правды.
    // Когда Room обновляется (add/remove), Flow автоматически эмитит новый список.
    val favourites: StateFlow<Set<Int>> get() = _favourites
    private val _favourites = MutableStateFlow<Set<Int>>(emptySet())

    private var searchJob: Job? = null

    init {
        // Подписываемся на Room Flow — любое изменение в БД сразу отражается в UI
        favouriteRepository.favouriteIds
            .onEach { ids ->
                _favourites.value = ids.toSet()
                // Синхронизируем favourites внутри текущего UiState
                val current = _uiState.value
                if (current is PokemonListUiState.Success) {
                    _uiState.value = current.copy(favourites = ids.toSet())
                }
            }
            .launchIn(viewModelScope)

        loadPokemonList()
    }

    fun onEvent(event: PokemonListEvent) {
        when (event) {
            is PokemonListEvent.Search -> performSearch(event.query)
            is PokemonListEvent.Retry -> loadPokemonList()
            is PokemonListEvent.Refresh -> {
                searchJob?.cancel()
                loadPokemonList()
            }
            is PokemonListEvent.ToggleFavourite -> toggleFavourite(event.pokemonId, event.pokemonName)
        }
    }

    private fun loadPokemonList() {
        viewModelScope.launch {
            _uiState.value = PokemonListUiState.Loading
            pokemonRepository.getPokemonList().fold(
                onSuccess = { list ->
                    _uiState.value = if (list.isEmpty()) PokemonListUiState.Empty
                    else PokemonListUiState.Success(pokemonList = list, favourites = _favourites.value)
                },
                onFailure = { error ->
                    _uiState.value = PokemonListUiState.Error(error.message ?: "Unknown error")
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
                    _uiState.value = if (results.isEmpty()) PokemonListUiState.Empty
                    else PokemonListUiState.Success(
                        pokemonList = results,
                        searchQuery = query,
                        favourites = _favourites.value
                    )
                },
                onFailure = { error ->
                    _uiState.value = PokemonListUiState.Error(error.message ?: "Search failed")
                }
            )
        }
    }

    private fun toggleFavourite(pokemonId: Int, pokemonName: String) {
        viewModelScope.launch {
            // Room сам уведомит Flow — ничего вручную обновлять не нужно
            favouriteRepository.toggleFavourite(pokemonId, pokemonName)
        }
    }
}

sealed interface PokemonListEvent {
    data class Search(val query: String) : PokemonListEvent
    data object Retry : PokemonListEvent
    data object Refresh : PokemonListEvent
    // pokemonName нужен для сохранения в Room
    data class ToggleFavourite(val pokemonId: Int, val pokemonName: String) : PokemonListEvent
}