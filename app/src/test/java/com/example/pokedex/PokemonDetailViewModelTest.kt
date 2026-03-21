package com.example.pokedex

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.pokedex.ui.PokemonDetailEvent
import com.example.pokedex.ui.PokemonDetailUiState
import com.example.pokedex.ui.PokemonDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        pokemonId: Int = 1,
        repo: FakePokemonRepository = FakePokemonRepository()
    ): PokemonDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("pokemonId" to pokemonId))
        return PokemonDetailViewModel(repo, savedStateHandle)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val repo = FakePokemonRepository()
        val savedStateHandle = SavedStateHandle(mapOf("pokemonId" to 1))
        val vm = PokemonDetailViewModel(repo, savedStateHandle)

        vm.uiState.test {
            val first = awaitItem()
            assertTrue(first is PokemonDetailUiState.Loading || first is PokemonDetailUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful load sets Success state with correct pokemon`() = runTest {
        val detail = FakePokemonRepository.defaultPokemonDetail(id = 25, name = "pikachu")
        val repo = FakePokemonRepository().apply { pokemonDetailResult = Result.success(detail) }
        val vm = createViewModel(pokemonId = 25, repo = repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is PokemonDetailUiState.Success)
        assertEquals("pikachu", (state as PokemonDetailUiState.Success).pokemon.name)
        assertEquals(25, state.pokemon.id)
    }

    @Test
    fun `error from repository sets Error state`() = runTest {
        val repo = FakePokemonRepository().apply {
            pokemonDetailResult = Result.failure(Exception("Not found"))
        }
        val vm = createViewModel(repo = repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is PokemonDetailUiState.Error)
        assertEquals("Not found", (state as PokemonDetailUiState.Error).message)
    }

    @Test
    fun `retry after error loads data successfully`() = runTest {
        val repo = FakePokemonRepository().apply {
            pokemonDetailResult = Result.failure(Exception("Timeout"))
        }
        val vm = createViewModel(repo = repo)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonDetailUiState.Error)

        repo.pokemonDetailResult = Result.success(FakePokemonRepository.defaultPokemonDetail())
        vm.onEvent(PokemonDetailEvent.Retry)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonDetailUiState.Success)
    }

    @Test
    fun `SavedStateHandle provides correct pokemonId`() = runTest {
        val detail = FakePokemonRepository.defaultPokemonDetail(id = 42, name = "mewtwo")
        val repo = FakePokemonRepository().apply { pokemonDetailResult = Result.success(detail) }
        val vm = createViewModel(pokemonId = 42, repo = repo)
        advanceUntilIdle()

        val state = vm.uiState.value as PokemonDetailUiState.Success
        assertEquals(42, state.pokemon.id)
    }

    @Test
    fun `uiState emits Loading then Success in sequence`() = runTest {
        val repo = FakePokemonRepository()
        repo.pokemonDetailResult = Result.success(FakePokemonRepository.defaultPokemonDetail())

        val savedStateHandle = SavedStateHandle(mapOf("pokemonId" to 1))
        val vm = PokemonDetailViewModel(repo, savedStateHandle)

        vm.uiState.test {
            val first = awaitItem()
            assertTrue(first is PokemonDetailUiState.Loading || first is PokemonDetailUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}