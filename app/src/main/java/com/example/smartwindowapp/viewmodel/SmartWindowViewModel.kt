package com.example.smartwindowapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.smartwindowapp.data.model.SensorStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.*
import kotlin.random.Random

class SmartWindowViewModel : ViewModel() {

    private val temperatureRange = 23f..24f
    private val humidityRange = 41f..50f
    private val co2Range = 350..1100
    private val pm25Range = 5..12

    private val _sensorStatus = MutableStateFlow<SensorStatus?>(null)
    val sensorStatus: StateFlow<SensorStatus?> = _sensorStatus

    private val mockScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var isInCo2Spike = false
    private var spikeDurationCount = 0
    private val spikeDurationMax = 3

    init {
        _sensorStatus.value = SensorStatus(
            temperature = 23.5f,
            humidity = 45f,
            co2 = 500,
            pm25 = 8
        )
        loadSmoothMockSensorStatus()
    }

    private fun loadSmoothMockSensorStatus() {
        mockScope.launch {
            while (isActive) {
                delay(5000L)

                val current = _sensorStatus.value ?: continue

                val newTemperature = smoothChange(current.temperature, temperatureRange, 0.3f)
                val newHumidity = smoothChange(current.humidity, humidityRange, 0.5f)
                val newCO2 = generateCO2(current.co2)
                val newPM25 = smoothChangeInt(current.pm25, pm25Range, 1)

                _sensorStatus.value = SensorStatus(
                    temperature = newTemperature,
                    humidity = newHumidity,
                    co2 = newCO2,
                    pm25 = newPM25
                )
            }
        }
    }

    private fun smoothChange(current: Float, range: ClosedFloatingPointRange<Float>, delta: Float): Float {
        val change = Random.nextFloat() * (delta * 2) - delta
        val newValue = (current + change).coerceIn(range)
        return String.format("%.1f", newValue).toFloat()
    }

    private fun smoothChangeInt(current: Int, range: IntRange, delta: Int): Int {
        val change = Random.nextInt(-delta, delta + 1)
        return (current + change).coerceIn(range)
    }

    private fun generateCO2(current: Int): Int {
        val spikeChance = 0.1f

        return when {
            isInCo2Spike -> {
                spikeDurationCount++
                if (spikeDurationCount >= spikeDurationMax) {
                    isInCo2Spike = false
                    spikeDurationCount = 0
                }
                Random.nextInt(1000, 1100)
            }

            current > 950 -> {
                (current - Random.nextInt(30, 60)).coerceAtLeast(co2Range.first)
            }

            Random.nextFloat() < spikeChance -> {
                isInCo2Spike = true
                spikeDurationCount = 0
                Random.nextInt(1000, 1100)
            }

            else -> {
                smoothChangeInt(current, co2Range, 50)
            }
        }
    }

    fun openWindow() {
        println("🪟 OPEN WINDOW (mock)")
    }

    fun closeWindow() {
        println("🪟 CLOSE WINDOW (mock)")
    }

    override fun onCleared() {
        super.onCleared()
        mockScope.cancel()
    }
}
