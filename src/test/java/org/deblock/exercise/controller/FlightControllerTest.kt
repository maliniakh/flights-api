package org.deblock.exercise.controller

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.deblock.exercise.service.FlightService
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.format.support.DefaultFormattingConversionService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class FlightControllerTest {

    private lateinit var mockMvc: MockMvc
    private val flightService: FlightService = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        val controller = FlightController(flightService)
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(FlightExceptionHandler())
            .setConversionService(DefaultFormattingConversionService().also { it.addConverter(StringToKotlinxLocalDateConverter()) })
            .build()
    }

    @Test
    fun `allow same departure and return date`() {
        mockMvc.get("/api/flights") {
            param("origin", "GPS")
            param("destination", "UIO")
            param("departureDate", LocalDate.now().toString())
            param("returnDate", LocalDate.now().toString())
            param("numberOfPassengers", "1")
        }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `reject if departureDate is after returnDate`() {
        mockMvc.get("/api/flights") {
            param("origin", "WAW")
            param("destination", "RDM")
            param("departureDate", LocalDate.now().plusDays(2).toString())
            param("returnDate", LocalDate.now().toString())
            param("numberOfPassengers", "1")
        }
            .andExpect { status { isBadRequest() } }
            .andExpect { jsonPath("$.msg", containsString("Departure date cannot be after return date")) }
    }
}
