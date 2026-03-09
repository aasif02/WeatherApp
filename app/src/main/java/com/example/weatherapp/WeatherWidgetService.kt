package com.example.weatherapp

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherWidgetService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    coroutineScope.launch {
                        try {
                            val response = weatherRepository.getWeatherDataByCoordinates(location.latitude, location.longitude)
                            if (response.isSuccessful) {
                                response.body()?.let { weatherData ->
                                    updateWidget(weatherData)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        stopSelf(startId)
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return START_NOT_STICKY
    }

    private fun updateWidget(weatherData: WeatherResponse) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisAppWidget = ComponentName(this, WeatherWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.widget_textview, "${weatherData.name}: ${weatherData.main.temp.toInt()}°C")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}