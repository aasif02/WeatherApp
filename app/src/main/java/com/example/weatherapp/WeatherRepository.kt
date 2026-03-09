package com.example.weatherapp

import com.example.weatherapp.BuildConfig

class WeatherRepository(private val weatherService: WeatherService) {

    private val apiKey = BuildConfig.API_KEY

    suspend fun getWeatherData(city: String) = weatherService.getWeatherData(city, apiKey)

    suspend fun getForecastData(city: String) = weatherService.getForecastData(city, apiKey)

    suspend fun getWeatherDataByCoordinates(lat: Double, lon: Double) = weatherService.getWeatherDataByCoordinates(lat, lon, apiKey)

    suspend fun getForecastDataByCoordinates(lat: Double, lon: Double) = weatherService.getForecastDataByCoordinates(lat, lon, apiKey)
}
