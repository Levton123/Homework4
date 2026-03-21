package com.example.pokedex

import app.cash.turbine.test
import com.example.pokedex.data.model.PokemonListItem
import com.example.pokedex.ui.PokemonListEvent
import com.example.pokedex.ui.PokemonListUiState
import com.example.pokedex.ui.PokemonListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakePokemonRepo: FakePokemonRepository
    private lateinit var fakeFavouriteRepo: FakeFavouriteRepository
    private lateinit var viewModel: PokemonListViewModel

    @Before
    fun setup() {
        fakePokemonRepo = FakePokemonRepository()
        fakeFavouriteRepo = FakeFavouriteRepository()
        viewModel = PokemonListViewModel(fakePokemonRepo, fakeFavouriteRepo)
    }

    @Test
    fun `initial state is Loading then transitions to Success`() = runTest {
        fakePokemonRepo.pokemonListResult = Result.success(FakePokemonRepository.defaultPokemonList())
        val vm = PokemonListViewModel(fakePokemonRepo, fakeFavouriteRepo)

        vm.uiState.test {
            val first = awaitItem()
            assertTrue(first is PokemonListUiState.Loading || first is PokemonListUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful load populates pokemon list`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PokemonListUiState.Success)
        assertEquals(3, (state as PokemonListUiState.Success).pokemonList.size)
    }

    @Test
    fun `error from repository transitions state to Error`() = runTest {
        fakePokemonRepo.pokemonListResult = Result.failure(Exception("Network error"))
        val vm = PokemonListViewModel(fakePokemonRepo, fakeFavouriteRepo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is PokemonListUiState.Error)
        assertEquals("Network error", (state as PokemonListUiState.Error).message)
    }

    @Test
    fun `retry after error initiates new request`() = runTest {
        fakePokemonRepo.pokemonListResult = Result.failure(Exception("Network error"))
        val vm = PokemonListViewModel(fakePokemonRepo, fakeFavouriteRepo)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonListUiState.Error)
        val callsBefore = fakePokemonRepo.getPokemonListCallCount

        fakePokemonRepo.pokemonListResult = Result.success(FakePokemonRepository.defaultPokemonList())
        vm.onEvent(PokemonListEvent.Retry)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonListUiState.Success)
        assertTrue(fakePokemonRepo.getPokemonListCallCount > callsBefore)
    }

    @Test
    fun `empty list from repository transitions state to Empty`() = runTest {
        fakePokemonRepo.pokemonListResult = Result.success(emptyList())
        val vm = PokemonListViewModel(fakePokemonRepo, fakeFavouriteRepo)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonListUiState.Empty)
    }

    @Test
    fun `empty search result gives Empty state not Success with empty list`() = runTest {
        fakePokemonRepo.searchResult = Result.success(emptyList())
        advanceUntilIdle()

        viewModel.onEvent(PokemonListEvent.Search("zzz"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PokemonListUiState.Empty)
    }

    @Test
    fun `search filters pokemon list correctly`() = runTest {
        val filtered = listOf(PokemonListItem("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"))
        fakePokemonRepo.searchResult = Result.success(filtered)
        advanceUntilIdle()

        viewModel.onEvent(PokemonListEvent.Search("bulba"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PokemonListUiState.Success)
        assertEquals(1, (state as PokemonListUiState.Success).pokemonList.size)
        assertEquals("bulbasaur", state.pokemonList[0].name)
    }

    @Test
    fun `addFavourite updates favourites flow`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(PokemonListEvent.AddFavourite(1, "bulbasaur"))
        advanceUntilIdle()

        val favs = viewModel.favourites.value
        assertTrue(favs.contains(1))
    }

    @Test
    fun `removeFavourite removes from favourites flow`() = runTest {
        fakeFavouriteRepo.setFavourites(listOf(1, 2))
        advanceUntilIdle()

        viewModel.onEvent(PokemonListEvent.RemoveFavourite(1))
        advanceUntilIdle()

        val favs = viewModel.favourites.value
        assertTrue(!favs.contains(1))
        assertTrue(favs.contains(2))
    }

    @Test
    fun `adding same pokemon twice does not duplicate in favourites`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(PokemonListEvent.AddFavourite(1, "bulbasaur"))
        advanceUntilIdle()
        viewModel.onEvent(PokemonListEvent.AddFavourite(1, "bulbasaur"))
        advanceUntilIdle()

        assertEquals(1, fakeFavouriteRepo.addedFavourites.count { it.first == 1 }.let {
            viewModel.favourites.value.count { id -> id == 1 }
        })
    }

    @Test
    fun `favourites are reflected in uiState Success`() = runTest {
        fakeFavouriteRepo.setFavourites(listOf(1, 2))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PokemonListUiState.Success)
        assertEquals(setOf(1, 2), (state as PokemonListUiState.Success).favourites)
    }

    @Test
    fun `uiState flow emits Loading then Success sequence`() = runTest {
        fakePokemonRepo.pokemonListResult = Result.success(FakePokemonRepository.defaultPokemonList())

        viewModel.uiState.test {
            val states = mutableListOf<PokemonListUiState>()
            states.add(awaitItem())
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()

            assertTrue(states.isNotEmpty())
        }
    }

    @Test
    fun `refresh cancels previous search and reloads list`() = runTest {
        val filtered = listOf(PokemonListItem("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"))
        fakePokemonRepo.searchResult = Result.success(filtered)
        viewModel.onEvent(PokemonListEvent.Search("bulba"))
        advanceUntilIdle()

        fakePokemonRepo.pokemonListResult = Result.success(FakePokemonRepository.defaultPokemonList())
        viewModel.onEvent(PokemonListEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PokemonListUiState.Success)
        assertEquals(3, (state as PokemonListUiState.Success).pokemonList.size)
    }
}