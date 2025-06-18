package com.example.smartwindowapp.data.remote

import com.example.smartwindowapp.data.model.SensorStatus
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/status")
    fun getSensorStatus(): Call<SensorStatus>

    @POST("/open")
    fun openWindow(): Call<Void>

    @POST("/close")
    fun closeWindow(): Call<Void>
}
