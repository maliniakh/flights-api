package org.deblock.exercise.controller

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import kotlinx.datetime.LocalDate
import org.deblock.exercise.model.FlightResponse
import org.deblock.exercise.service.FlightService
import org.springframework.core.convert.converter.Converter
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/flights")
@Validated
class FlightController(
    private val flightService: FlightService
) {
    @GetMapping
    fun searchFlights(
        @RequestParam @NotBlank origin: String,
        @RequestParam @NotBlank destination: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) departureDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) returnDate: LocalDate,
        @RequestParam @Min(1) numberOfPassengers: Int
    ): List<FlightResponse> {
        require(departureDate <= returnDate) { "Departure date cannot be after return date" }

        return flightService.searchFlights(origin, destination, departureDate, returnDate, numberOfPassengers)
    }
}

@Component
class StringToKotlinxLocalDateConverter : Converter<String, LocalDate> {
    override fun convert(source: String): LocalDate {
        return LocalDate.parse(source)
    }
}