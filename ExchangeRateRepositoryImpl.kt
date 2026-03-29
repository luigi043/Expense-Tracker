package com.expensetracker.data.remote.repository

import com.expensetracker.BuildConfig
import com.expensetracker.data.remote.api.ExchangeRateApi
import com.expensetracker.domain.model.ExchangeRate
import com.expensetracker.domain.repository.ExchangeRateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: ExchangeRateApi
) : ExchangeRateRepository {

    // In-memory cache valid for 1 hour
    private var cachedRate: ExchangeRate? = null
    private val cacheValidityMs = 3_600_000L

    override suspend fun getLatestRates(base: String): Result<ExchangeRate> = runCatching {
        val response = api.getLatestRates(
            apiKey = BuildConfig.EXCHANGE_RATE_API_KEY,
            base = base
        )
        val rate = ExchangeRate(
            base = response.base,
            rates = response.rates,
            timestamp = response.timestamp * 1000L
        )
        cachedRate = rate
        rate
    }

    override suspend fun getCachedRates(): ExchangeRate? {
        val cached = cachedRate ?: return null
        return if (System.currentTimeMillis() - cached.timestamp < cacheValidityMs) cached else null
    }

    override suspend fun convert(amount: Double, from: String, to: String): Double? {
        val rates = (getCachedRates() ?: getLatestRates(from).getOrNull()) ?: return null
        // All rates are relative to base; convert via base
        val fromRate = if (from == rates.base) 1.0 else rates.rates[from] ?: return null
        val toRate = rates.rates[to] ?: return null
        return (amount / fromRate) * toRate
    }
}