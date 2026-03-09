package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val weatherService by lazy {
        retrofit.create(WeatherService::class.java)
    }

    private val weatherRepository by lazy {
        WeatherRepository(weatherService)
    }

    private val weatherViewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory(weatherRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHourlyForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvDailyForecast.layoutManager = LinearLayoutManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnSearch.setOnClickListener {
            val city = binding.etCityName.text.toString()
            if (city.isNotEmpty()) {
                weatherViewModel.fetchWeatherData(city)
                weatherViewModel.fetchForecastData(city)
            }
        }

        binding.etCityName.addTextChangedListener {
            binding.tvError.visibility = View.GONE
        }

        observeViewModel()

        checkLocationPermission()
    }

    private fun observeViewModel() {
        weatherViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.mainContent.visibility = View.GONE
                binding.tvError.visibility = View.GONE
            }
        }

        weatherViewModel.error.observe(this) { error ->
            if (error != null && error.isNotEmpty()) {
                binding.mainContent.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = error
            } else {
                binding.tvError.visibility = View.GONE
            }
        }

        weatherViewModel.weatherData.observe(this) { weatherData ->
            binding.mainContent.visibility = View.VISIBLE
            updateWeatherUI(weatherData)
        }

        weatherViewModel.forecastData.observe(this) { forecastData ->
            updateForecastUI(forecastData)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            getLastLocation()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                // Permission denied
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    weatherViewModel.fetchWeatherData(location.latitude, location.longitude)
                    weatherViewModel.fetchForecastData(location.latitude, location.longitude)
                }
            }
    }

    private fun updateWeatherUI(weatherData: WeatherResponse?) {
        weatherData?.let {
            binding.tvCityName.text = it.name
            binding.tvTemperature.text = "${it.main.temp.toInt()}°C"
            binding.tvDescription.text = it.weather.firstOrNull()?.description?.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
            binding.tvHumidity.text = "Humidity: ${it.main.humidity}%"
            binding.tvWindSpeed.text = "Wind: ${it.wind.speed} m/s"
            binding.tvSunrise.text = "Sunrise: ${formatTime(it.sys.sunrise)}"
            binding.tvSunset.text = "Sunset: ${formatTime(it.sys.sunset)}"

            it.weather.firstOrNull()?.icon?.let {
                icon ->
                val iconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
                Glide.with(this).load(iconUrl).into(binding.ivWeatherIcon)
            }
        }
    }

    private fun updateForecastUI(forecastData: ForecastResponse?) {
        forecastData?.list?.let {
            val hourlyForecast = it.take(8)
            val dailyForecast = it.filter { item -> item.dtTxt.contains("12:00:00") }

            binding.rvHourlyForecast.adapter = ForecastAdapter(hourlyForecast, ForecastType.HOURLY)
            binding.rvDailyForecast.adapter = ForecastAdapter(dailyForecast, ForecastType.DAILY)
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = Date(timestamp * 1000)
        return sdf.format(date)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}