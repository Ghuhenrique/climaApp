# ClimaApp - Avaliação I (Programação para Dispositivos Móveis / UEG)

Kotlin | Jetpack Compose | MVVM | Retrofit + Kotlinx Serialization | Coroutines | Koin | OpenWeatherMap

## Como rodar
1. Crie uma chave grátis em https://openweathermap.org/api (pode levar até 2 horas para ativar).
2. Copie `local.properties.example` para `local.properties` e preencha `OPENWEATHER_API_KEY`
   (o Android Studio também adiciona `sdk.dir` nesse arquivo).
3. Abra a pasta no Android Studio, aguarde o Gradle Sync e execute.

## Funcionalidades
- Lista (LazyColumn) com o clima de 15 capitais, carregadas em paralelo
- Indicador de carregamento; mensagem amigável + "Tentar Novamente" em erro
- Busca: ao digitar, filtra a lista; ao tocar na lupa (ou "Buscar" no teclado), consulta a API
  e adiciona QUALQUER cidade do mundo no topo da lista

## Estrutura
- `data/remote`      -> Retrofit (`WeatherApi`) e DTOs
- `data/repository`  -> Model: `WeatherRepository` e `Weather`
- `ui`               -> View (`WeatherScreen`, `MainActivity`) e `WeatherViewModel`
- `di`               -> módulo Koin
