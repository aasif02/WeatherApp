package com.example.weatherapp

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String,
    val wind: Wind,
    val sys: Sys
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Wind(
    val speed: Double
)

data class Sys(
    val sunrise: Long,
    val sunset: Long
)

data class Weather(
    val description: String,
    val icon: String
)