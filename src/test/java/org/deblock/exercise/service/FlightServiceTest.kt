package org.deblock.exercise.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import kotlinx.datetime.*
import org.deblock.exercise.client.ApiClient
import org.deblock.exercise.model.FlightResponse
import org.deblock.exercise.model.SupplierEnum
import org.deblock.exercise.util.now
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class FlightServiceTest {

    private lateinit var flightService: FlightService
    private val apiClient1: ApiClient = mockk()
    private val apiClient2: ApiClient = mockk()

    @BeforeEach
    fun setup() {
        flightService = FlightService(listOf(apiClient1, apiClient2))
    }

    @Test
    fun `aggregate and sort flights from multiple providers`() {
        val flight1 = FlightResponse(
            "LOT", SupplierEnum.CRAZY_AIR.prettyName, BigDecimal(200.0), "JFK", "LAX",
            LocalDateTime.parse("2025-03-18T10:00:00"), LocalDateTime.parse("2025-03-19T10:00:00")
        )
        val flight2 = FlightResponse(
            "Lufthansa", SupplierEnum.CRAZY_AIR.prettyName, BigDecimal(180.0), "JFK", "LAX",
            LocalDateTime.parse("2025-03-18T12:00:00"), LocalDateTime.parse("2025-03-19T12:00:00")
        )
        val flight3 = FlightResponse(
            "KLM", SupplierEnum.TOUGH_JET.prettyName, BigDecimal(220.0), "JFK", "LAX",
            LocalDateTime.parse("2025-03-18T12:00:00"), LocalDateTime.parse("2025-03-19T12:00:00")
        )

        val today = LocalDateTime.now().date
        val returnDate = today + DatePeriod(days = 5)
        val passengers = 1

        every { apiClient1.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) } returns listOf(flight1, flight3)
        every { apiClient2.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) } returns listOf(flight2)

        val result = flightService.searchFlights("JFK", "LAX", today, returnDate, passengers)

        assertEquals(3, result.size)
        assertEquals("Lufthansa", result[0].airline)
        assertEquals("LOT", result[1].airline)
        assertEquals("KLM", result[2].airline)

        verify { apiClient1.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) }
        verify { apiClient2.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) }
    }

    @Test
    fun `should return empty list when no flights available`() {
        val today = LocalDateTime.now().date
        val returnDate = today + DatePeriod(days = 5)
        val passengers = 1

        every { apiClient1.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) } returns emptyList()
        every { apiClient2.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) } returns emptyList()

        val result = flightService.searchFlights("JFK", "LAX", today, returnDate, passengers)

        assertTrue(result.isEmpty())

        verify { apiClient1.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) }
        verify { apiClient2.queryFlights(eq("JFK"), eq("LAX"), eq(today), eq(returnDate), eq(passengers)) }
    }
}
