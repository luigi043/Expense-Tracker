package com.expensetracker.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

// Uses exchangeratesapi.io — free tier supports EUR as base currency.
// Replace with https://api.exchangerate-api.com for USD base (free, no key needed).
interface ExchangeRateApi {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("access_key") apiKey: String,
        @Query("base") base: String = "EUR",
        @Query("symbols") symbols: String? = null   // null = all currencies
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val success: Boolean,
    val timestamp: Long,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)