package com.example.pokedex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>
)

@Serializable
data class PokemonListItem(
    val name: String,
    val url: String
) {
    val id: Int
        get() = url.trimEnd('/').split('/').last().toInt()
}

@Serializable
data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: PokemonSprites,
    val types: List<PokemonTypeSlot>,
    val abilities: List<PokemonAbilitySlot>,
    val stats: List<PokemonStat>
)

@Serializable
data class PokemonSprites(
    @SerialName("front_default")
    val frontDefault: String?,
    @SerialName("front_shiny")
    val frontShiny: String?,
    val other: OtherSprites? = null
)

@Serializable
data class OtherSprites(
    @SerialName("official-artwork")
    val officialArtwork: OfficialArtwork? = null
)

@Serializable
data class OfficialArtwork(
    @SerialName("front_default")
    val frontDefault: String?
)

@Serializable
data class PokemonTypeSlot(
    val slot: Int,
    val type: PokemonType
)

@Serializable
data class PokemonType(
    val name: String,
    val url: String
)

@Serializable
data class PokemonAbilitySlot(
    val slot: Int,
    val ability: PokemonAbility,
    @SerialName("is_hidden")
    val isHidden: Boolean
)

@Serializable
data class PokemonAbility(
    val name: String,
    val url: String
)

@Serializable
data class PokemonStat(
    @SerialName("base_stat")
    val baseStat: Int,
    val effort: Int,
    val stat: StatInfo
)

@Serializable
data class StatInfo(
    val name: String,
    val url: String
)