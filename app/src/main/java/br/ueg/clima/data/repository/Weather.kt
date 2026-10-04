package br.ueg.clima.data.repository

/** Objeto de domínio "limpo" exposto à ViewModel. */
data class Weather(
    val id: Int,
    val city: String,
    val country: String,
    val temperature: Int,
    val feelsLike: Int,
    val humidity: Int,
    val windKmh: Int,
    val description: String,
    val iconUrl: String
)
