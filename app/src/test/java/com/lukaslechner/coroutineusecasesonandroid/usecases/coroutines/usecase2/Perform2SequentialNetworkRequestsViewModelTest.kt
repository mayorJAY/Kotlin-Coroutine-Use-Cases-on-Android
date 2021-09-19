package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lukaslechner.coroutineusecasesonandroid.mock.mockVersionFeaturesAndroid10
import com.lukaslechner.coroutineusecasesonandroid.utils.MainCoroutineScopeRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class Perform2SequentialNetworkRequestsViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get: Rule
    val mainCoroutineScopeRule: MainCoroutineScopeRule = MainCoroutineScopeRule()

    private val receivedUiStates = mutableListOf<UiState>()

    @Test
    fun `should return Success when both network requests are successful`() {
        mainCoroutineScopeRule.runBlockingTest {
            val fakeSuccessApi = FakeSuccessApi()
            val sut = Perform2SequentialNetworkRequestsViewModel(fakeSuccessApi)
            sut.observe()
            Assert.assertTrue(receivedUiStates.isEmpty())
            sut.perform2SequentialNetworkRequest()
            Assert.assertEquals(listOf(UiState.Loading, UiState.Success(mockVersionFeaturesAndroid10)), receivedUiStates)
            Assert.assertEquals(mockVersionFeaturesAndroid10, (receivedUiStates[1] as UiState.Success).versionFeatures)
        }
    }

    @Test
    fun `should return Error when first network requests fails`() {
        mainCoroutineScopeRule.runBlockingTest {
            val fakeVersionsErrorApi = FakeVersionsErrorApi()
            val sut = Perform2SequentialNetworkRequestsViewModel(fakeVersionsErrorApi)
            sut.observe()
            Assert.assertTrue(receivedUiStates.isEmpty())
            sut.perform2SequentialNetworkRequest()
            Assert.assertEquals(listOf(UiState.Loading, UiState.Error("Network Request failed!")), receivedUiStates)
            Assert.assertFalse(receivedUiStates.contains(UiState.Success(mockVersionFeaturesAndroid10)))
        }
    }

    @Test
    fun `should return Error when second network requests fails`() {
        mainCoroutineScopeRule.runBlockingTest {
            val fakeFeaturesErrorApi = FakeFeaturesErrorApi()
            val sut = Perform2SequentialNetworkRequestsViewModel(fakeFeaturesErrorApi)
            sut.observe()
            Assert.assertTrue(receivedUiStates.isEmpty())
            sut.perform2SequentialNetworkRequest()
            Assert.assertEquals(listOf(UiState.Loading, UiState.Error("Network Request failed!")), receivedUiStates)
            Assert.assertFalse(receivedUiStates.contains(UiState.Success(mockVersionFeaturesAndroid10)))
        }
    }

    private fun Perform2SequentialNetworkRequestsViewModel.observe() {
        uiState().observeForever { uiState ->
            if (uiState != null) {
                receivedUiStates.add(uiState)
            }
        }
    }
}