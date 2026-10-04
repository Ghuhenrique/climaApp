package br.ueg.clima.di

import br.ueg.clima.BuildConfig
import br.ueg.clima.data.remote.WeatherApi
import br.ueg.clima.data.repository.WeatherRepository
import br.ueg.clima.ui.WeatherViewModel
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule = module {
    single { Json { ignoreUnknownKeys = true } }

    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single<WeatherApi> { get<Retrofit>().create(WeatherApi::class.java) }

    single { WeatherRepository(get(), BuildConfig.OPENWEATHER_API_KEY) }

    viewModel { WeatherViewModel(get()) }
}
