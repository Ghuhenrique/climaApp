package br.ueg.clima.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val id: Int,
    val name: String,
    val main: MainDto,
    val weather: List<ConditionDto>,
    val wind: WindDto = WindDto(),
    val sys: SysDto = SysDto()
)

@Serializable
data class MainDto(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    val humidity: Int
)

@Serializable
data class ConditionDto(val description: String, val icon: String)

@Serializable
data class WindDto(val speed: Double = 0.0)

@Serializable
data class SysDto(val country: String = "")
