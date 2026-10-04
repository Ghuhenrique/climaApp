package br.ueg.clima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.ueg.clima.data.repository.Weather
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchStatus by viewModel.searchStatus.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clima pelo Mundo") }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchField(
                query = query,
                onChange = viewModel::onQueryChange,
                onSearch = viewModel::searchCity
            )

            when (val s = searchStatus) {
                is SearchStatus.Searching ->
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                is SearchStatus.Failed ->
                    Text(
                        s.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                is SearchStatus.Idle -> Unit
            }

            when (val s = state) {
                is WeatherUiState.Loading -> LoadingContent()
                is WeatherUiState.Error -> ErrorContent(s.message, viewModel::load)
                is WeatherUiState.Success -> {
                    if (s.data.isEmpty()) EmptyContent(query, viewModel::searchCity)
                    else WeatherList(s.data)
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, onSearch: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Digite uma cidade e toque na lupa") },
        leadingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Buscar cidade")
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Carregando previsões...")
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ops! 😕", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Tentar Novamente") }
        }
    }
}

/** Aparece quando o texto digitado não bate com nenhuma cidade da lista. */
@Composable
private fun EmptyContent(query: String, onSearch: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nenhuma cidade da lista combina com \"$query\".", textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSearch) { Text("Buscar \"$query\" no mundo todo") }
        }
    }
}

@Composable
private fun WeatherList(items: List<Weather>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { WeatherCard(it) }
    }
}

@Composable
private fun WeatherCard(w: Weather) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(model = w.iconUrl, contentDescription = w.description, modifier = Modifier.size(64.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (w.country.isNotEmpty()) "${w.city} (${w.country})" else w.city,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(w.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Sensação ${w.feelsLike}° • Umidade ${w.humidity}% • Vento ${w.windKmh} km/h",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("${w.temperature}°C", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }
    }
}
