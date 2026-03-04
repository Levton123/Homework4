package com.example.pokedex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.ui.screen.FavouritesScreen
import com.example.pokedex.ui.screen.PokemonDetailScreen
import com.example.pokedex.ui.screen.PokemonListScreen
import com.example.pokedex.ui.viewmodel.PokemonDetailEvent
import com.example.pokedex.ui.viewmodel.PokemonDetailViewModel
import com.example.pokedex.ui.viewmodel.PokemonListEvent
import com.example.pokedex.ui.viewmodel.PokemonListViewModel

sealed class Screen(val route: String) {
    data object PokemonList : Screen("pokemon_list")
    data object PokemonDetail : Screen("pokemon_detail/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "pokemon_detail/$pokemonId"
    }
    data object Favourites : Screen("favourites")
}

@Composable
fun PokemonNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Hilt сам управляет lifecycle VM — shared VM живёт пока жив NavHost
    val sharedListViewModel: PokemonListViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.PokemonList.route
    ) {
        composable(Screen.PokemonList.route) {
            val uiState by sharedListViewModel.uiState.collectAsState()

            PokemonListScreen(
                uiState = uiState,
                onEvent = sharedListViewModel::onEvent,
                onPokemonClick = { pokemonId ->
                    navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                },
                onFavouritesClick = {
                    navController.navigate(Screen.Favourites.route)
                }
            )
        }

        composable(
            route = Screen.PokemonDetail.route,
            arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pokemonId = backStackEntry.arguments?.getInt("pokemonId") ?: return@composable

            // Hilt читает pokemonId из SavedStateHandle автоматически
            val detailViewModel: PokemonDetailViewModel = hiltViewModel()
            val detailUiState by detailViewModel.uiState.collectAsState()
            val favourites by sharedListViewModel.favourites.collectAsState()
            val isFavourite = favourites.contains(pokemonId)

            // Нужно имя для сохранения в Room — берём из детального состояния
            val pokemonName = (detailUiState as? com.example.pokedex.ui.state.PokemonDetailUiState.Success)
                ?.pokemon?.name ?: ""

            PokemonDetailScreen(
                uiState = detailUiState,
                isFavourite = isFavourite,
                onEvent = { event ->
                    when (event) {
                        is PokemonDetailEvent.ToggleFavourite ->
                            sharedListViewModel.onEvent(
                                PokemonListEvent.ToggleFavourite(pokemonId, pokemonName)
                            )
                        else -> detailViewModel.onEvent(event)
                    }
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.Favourites.route) {
            val favourites by sharedListViewModel.favourites.collectAsState()

            FavouritesScreen(
                favouriteIds = favourites,
                onPokemonClick = { pokemonId ->
                    navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                },
                onRemoveFavourite = { pokemonId ->
                    // Для удаления имя не нужно — toggleFavourite проверит isFavourite в Room
                    sharedListViewModel.onEvent(PokemonListEvent.ToggleFavourite(pokemonId, ""))
                },
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}