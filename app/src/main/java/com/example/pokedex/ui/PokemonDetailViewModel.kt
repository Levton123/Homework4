package com.example.pokedex.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    init {
        loadPokemonDetail()
    }

    fun onEvent(event: PokemonDetailEvent) {
        when (event) {
            is PokemonDetailEvent.Retry -> loadPokemonDetail()
            is PokemonDetailEvent.ToggleFavourite -> Unit
        }
    }

    private fun loadPokemonDetail() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            repository.getPokemonDetail(pokemonId).fold(
                onSuccess = { pokemon -> _uiState.value = PokemonDetailUiState.Success(pokemon) },
                onFailure = { error -> _uiState.value = PokemonDetailUiState.Error(error.message ?: "Failed to load Pokemon details") }
            )
        }
    }
}

sealed interface PokemonDetailEvent {
    data object Retry : PokemonDetailEvent
    data object ToggleFavourite : PokemonDetailEvent
}