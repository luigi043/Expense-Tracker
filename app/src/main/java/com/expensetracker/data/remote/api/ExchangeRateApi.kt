package com.expensetracker.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("access_key") apiKey: String,
        @Query("base") base: String = "EUR",
        @Query("symbols") symbols: String? = null
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val success: Boolean,
    val timestamp: Long,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)