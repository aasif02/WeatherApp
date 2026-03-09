package com.example.weatherapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

@ExperimentalCoroutinesApi
class WeatherViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var weatherRepository: WeatherRepository

    @Mock
    private lateinit var weatherObserver: Observer<WeatherResponse>

    @Mock
    private lateinit var loadingObserver: Observer<Boolean>

    @Mock
    private lateinit var errorObserver: Observer<String>

    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherViewModel(weatherRepository)

        viewModel.weatherData.observeForever(weatherObserver)
        viewModel.isLoading.observeForever(loadingObserver)
        viewModel.error.observeForever(errorObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `fetchWeatherData success`() = runTest {
        // Given
        val mockWeatherResponse = WeatherResponse(
            name = "Test City",
            main = Main(temp = 25.0, humidity = 80, pressure = 1012),
            weather = listOf(Weather(description = "clear sky", icon = "01d")),
            wind = Wind(speed = 5.0),
            sys = Sys(sunrise = 1621558800, sunset = 1621609200)
        )
        val successResponse = Response.success(mockWeatherResponse)

        whenever(weatherRepository.getWeatherData("Test City")).thenReturn(successResponse)

        // When
        viewModel.fetchWeatherData("Test City")

        // Then
        verify(loadingObserver).onChanged(true)
        verify(weatherObserver).onChanged(mockWeatherResponse)
        verify(loadingObserver).onChanged(false)
    }

    @Test
    fun `fetchWeatherData failure`() = runTest {
        // Given
        whenever(weatherRepository.getWeatherData("Test City")).thenThrow(RuntimeException("Network Error"))

        // When
        viewModel.fetchWeatherData("Test City")

        // Then
        verify(loadingObserver).onChanged(true)
        verify(errorObserver).onChanged("Error fetching weather data")
        verify(loadingObserver).onChanged(false)
        verify(weatherObserver, never()).onChanged(org.mockito.kotlin.any())
    }
}