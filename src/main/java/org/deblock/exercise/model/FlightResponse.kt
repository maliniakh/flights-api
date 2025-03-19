package org.deblock.exercise.model

import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

data class FlightResponse(
    val airline: String,
    val supplier: String,
    val fare: BigDecimal,
    val departureAirportCode: String,
    val destinationAirportCode: String,
    val departureDate: LocalDateTime,
    val arrivalDate: LocalDateTime
)
