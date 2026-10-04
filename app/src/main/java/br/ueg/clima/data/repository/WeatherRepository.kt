package br.ueg.clima.data.repository

import br.ueg.clima.data.remote.WeatherApi
import br.ueg.clima.data.remote.WeatherResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

class WeatherRepository(
    private val api: WeatherApi,
    private val apiKey: String
) {
    // Cidades que aparecem logo que o app abre.
    private val defaultCities = listOf(
        "Goiânia", "Brasília", "São Paulo", "Rio de Janeiro", "Belo Horizonte",
        "Rio Branco", "Fortaleza", "Recife", "Curitiba", "Porto Alegre",
        "Manaus", "Belém", "Florianópolis", "Cuiabá", "Campo Grande"
    )

    /** Lista inicial. Só falha se NENHUMA cidade puder ser carregada. */
    suspend fun getWeatherList(): Result<List<Weather>> = safeCall {
        requireApiKey()
        val results = coroutineScope {
            defaultCities.map { city ->
                async { safeCall { api.getWeather(city, apiKey).toDomain() } }
            }.awaitAll()
        }
        val loaded = results.mapNotNull { it.getOrNull() }
        if (loaded.isEmpty()) {
            throw results.first().exceptionOrNull() ?: IllegalStateException("Sem dados")
        }
        loaded
    }

    /** Busca o clima de QUALQUER cidade pelo nome digitado. */
    suspend fun searchCity(name: String): Result<Weather> = safeCall {
        requireApiKey()
        api.getWeather(name.trim(), apiKey).toDomain()
    }

    private fun requireApiKey() {
        require(apiKey.isNotBlank()) { "Chave da API não configurada (local.properties)." }
    }

    /**
     * Igual ao runCatching, mas NÃO engole o cancelamento de coroutines
     * (boa prática: quem cancela precisa ser obedecido).
     */
    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun WeatherResponse.toDomain() = Weather(
        id = id,
        city = name,
        country = sys.country,
        temperature = main.temp.roundToInt(),
        feelsLike = main.feelsLike.roundToInt(),
        humidity = main.humidity,
        windKmh = (wind.speed * 3.6).roundToInt(),
        description = weather.firstOrNull()?.description.orEmpty().replaceFirstChar { it.uppercase() },
        iconUrl = "https://openweathermap.org/img/wn/${weather.firstOrNull()?.icon ?: "01d"}@2x.png"
    )
}
