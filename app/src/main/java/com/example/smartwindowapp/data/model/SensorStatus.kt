package com.example.smartwindowapp.data.model

data class SensorStatus(
    val temperature: Float,
    val humidity: Float,
    val co2: Int,
    val pm25: Int
)
