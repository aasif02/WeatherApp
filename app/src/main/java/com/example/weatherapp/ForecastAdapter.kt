package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ItemForecastBinding
import java.text.SimpleDateFormat
import java.util.Locale

enum class ForecastType {
    HOURLY,
    DAILY
}

class ForecastAdapter(
    private val forecastItems: List<ForecastItem>,
    private val forecastType: ForecastType
) : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val hourlyFormat = SimpleDateFormat("h a", Locale.getDefault())
    private val dailyFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    inner class ViewHolder(val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecastItem = forecastItems[position]
        holder.binding.tvTemp.text = "${forecastItem.main.temp.toInt()}°C"

        val date = inputFormat.parse(forecastItem.dtTxt)
        if (date != null) {
            holder.binding.tvTime.text = when (forecastType) {
                ForecastType.HOURLY -> hourlyFormat.format(date)
                ForecastType.DAILY -> dailyFormat.format(date)
            }
        } else {
            holder.binding.tvTime.text = forecastItem.dtTxt
        }

        val iconUrl = "https://openweathermap.org/img/wn/${forecastItem.weather.firstOrNull()?.icon}@2x.png"
        Glide.with(holder.itemView.context)
            .load(iconUrl)
            .into(holder.binding.ivWeatherIcon)
    }

    override fun getItemCount(): Int {
        return forecastItems.size
    }
}