package com.example.weatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData

    private val _forecastData = MutableLiveData<ForecastResponse>()
    val forecastData: LiveData<ForecastResponse> = _forecastData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchWeatherData(city: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val response = weatherRepository.getWeatherData(city)
                if (response.isSuccessful) {
                    _weatherData.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
            _isLoading.postValue(false)
        }
    }

    fun fetchWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val response = weatherRepository.getWeatherDataByCoordinates(lat, lon)
                if (response.isSuccessful) {
                    _weatherData.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
            _isLoading.postValue(false)
        }
    }

    fun fetchForecastData(city: String) {
        viewModelScope.launch {
            try {
                val response = weatherRepository.getForecastData(city)
                if (response.isSuccessful) {
                    _forecastData.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun fetchForecastData(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = weatherRepository.getForecastDataByCoordinates(lat, lon)
                if (response.isSuccessful) {
                    _forecastData.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }
}