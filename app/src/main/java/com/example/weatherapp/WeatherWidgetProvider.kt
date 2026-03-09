package com.example.weatherapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Start the service to update the widgets
        context.startService(Intent(context, WeatherWidgetService::class.java))
    }

    override fun onEnabled(context: Context) {
        // When the first widget is created, start the service to update it
        context.startService(Intent(context, WeatherWidgetService::class.java))
    }
}