package br.ueg.clima.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ueg.clima.data.repository.Weather
import br.ueg.clima.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/** Os três estados da tela principal. */
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val data: List<Weather>) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

/** Situação da busca de uma cidade nova (mostrada logo abaixo do campo de texto). */
sealed interface SearchStatus {
    data object Idle : SearchStatus
    data object Searching : SearchStatus
    data class Failed(val message: String) : SearchStatus
}

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchStatus = MutableStateFlow<SearchStatus>(SearchStatus.Idle)
    val searchStatus: StateFlow<SearchStatus> = _searchStatus.asStateFlow()

    /** Estado final da tela: a lista já filtrada pelo que o usuário está digitando. */
    val uiState: StateFlow<WeatherUiState> = combine(_state, _query) { state, q ->
        if (state is WeatherUiState.Success && q.isNotBlank()) {
            WeatherUiState.Success(state.data.filter { it.city.contains(q.trim(), ignoreCase = true) })
        } else state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeatherUiState.Loading)

    init { load() }

    fun onQueryChange(value: String) {
        _query.value = value
        if (_searchStatus.value is SearchStatus.Failed) _searchStatus.value = SearchStatus.Idle
    }

    /** Carrega a lista inicial de cidades. */
    fun load() {
        _state.value = WeatherUiState.Loading
        viewModelScope.launch {
            repository.getWeatherList()
                .onSuccess { _state.value = WeatherUiState.Success(it) }
                .onFailure { _state.value = WeatherUiState.Error(it.toFriendlyMessage()) }
        }
    }

    /** Busca na internet QUALQUER cidade digitada e a coloca no topo da lista. */
    fun searchCity() {
        val name = _query.value.trim()
        if (name.isEmpty()) return
        if (_searchStatus.value is SearchStatus.Searching) return
        if (_state.value is WeatherUiState.Loading) return

        _searchStatus.value = SearchStatus.Searching
        viewModelScope.launch {
            repository.searchCity(name)
                .onSuccess { found ->
                    val current = (_state.value as? WeatherUiState.Success)?.data.orEmpty()
                    // Coloca a cidade nova no topo, sem duplicar se ela já estava na lista.
                    _state.value = WeatherUiState.Success(listOf(found) + current.filter { it.id != found.id })
                    _query.value = ""
                    _searchStatus.value = SearchStatus.Idle
                }
                .onFailure { _searchStatus.value = SearchStatus.Failed(it.toSearchMessage(name)) }
        }
    }

    private fun Throwable.toFriendlyMessage() = when (this) {
        is IOException -> "Sem conexão com a internet. Verifique sua rede e tente novamente."
        is HttpException -> when (code()) {
            401 -> "Chave da API inválida ou ainda não ativada."
            429 -> "Limite de requisições excedido. Aguarde um pouco."
            else -> "Erro no servidor (HTTP ${code()})."
        }
        is IllegalArgumentException -> message ?: "Configuração inválida."
        else -> "Algo deu errado. Tente novamente."
    }

    private fun Throwable.toSearchMessage(name: String) = when {
        this is HttpException && code() == 404 ->
            "Não encontramos \"$name\". Tente sem acento ou em inglês (ex.: London)."
        else -> toFriendlyMessage()
    }
}
