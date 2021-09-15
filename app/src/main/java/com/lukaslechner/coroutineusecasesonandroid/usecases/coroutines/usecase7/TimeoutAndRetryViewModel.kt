package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase7

import androidx.lifecycle.viewModelScope
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.MockApi
import kotlinx.coroutines.*
import timber.log.Timber

class TimeoutAndRetryViewModel(
    private val api: MockApi = mockApi()
) : BaseViewModel<UiState>() {

    fun performNetworkRequest() {
        uiState.value = UiState.Loading
        val numberOfRetries = 2
        val timeout = 1000L

        // run api.getAndroidVersionFeatures(27) and api.getAndroidVersionFeatures(28) in parallel
        val deferred1 = viewModelScope.async {
            retryWithTimeout(numberOfRetries, timeout) {
                api.getAndroidVersionFeatures(27)
            }
        }

        val deferred2 = viewModelScope.async {
            retryWithTimeout(numberOfRetries, timeout) {
                api.getAndroidVersionFeatures(28)
            }
        }

        viewModelScope.launch {
            try {
                val versions = listOf(deferred1, deferred2).awaitAll()
                uiState.value = UiState.Success(versions)
            } catch (ex: Exception) {
                Timber.e(ex)
                uiState.value = UiState.Error("Network Request failed")
            }
        }
    }

    private suspend fun <T> retryWithTimeout(numberOfRetries: Int, timeout: Long, block: suspend () -> T) = retry(numberOfRetries) {
        withTimeout(timeout) {
            block()
        }
    }

    private suspend fun <T> retry(times: Int, initialDelayMillis: Long = 100, block: suspend () -> T): T {
        repeat(times) {
            try {
                return block()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
            delay(initialDelayMillis)
        }
        return block() // last attempt
    }
}