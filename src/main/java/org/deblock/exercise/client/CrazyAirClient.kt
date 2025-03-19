package org.deblock.exercise.client

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.deblock.exercise.model.FlightResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.deblock.exercise.model.SupplierEnum
import java.math.BigDecimal
import java.math.RoundingMode


@Component
class CrazyAirClient(restTemplate: RestTemplate) : ApiClient(restTemplate) {
    // might be injected from properties, but it's unlikely to be changed, I prefer it here
    override val baseUrl: String = "https://crazyair.com"

    override fun buildRequestUri(
        origin: String,
        destination: String,
        departureDate: LocalDate,
        returnDate: LocalDate,
        numberOfPassengers: Int
    ): String {
        return "?origin=$origin,destination=$destination,departureDate=$departureDate,returnDate=$returnDate,passengerCount=$numberOfPassengers"
    }

    override fun translateResponse(response: ResponseEntity<String>): List<FlightResponse> {
        return Json.decodeFromString<List<CrazyAirResponse>>(response.body!!)
            .map { FlightResponse(
                airline = it.airline, supplier = SupplierEnum.CRAZY_AIR.prettyName,
                fare = BigDecimal.valueOf(it.price).setScale(2, RoundingMode.HALF_UP),
                departureAirportCode = it.departureAirportCode, destinationAirportCode = it.destinationAirportCode,
                departureDate = it.departureDate, arrivalDate = it.arrivalDate)
            }
    }
}

@Serializable
private data class CrazyAirResponse(
    val airline: String,
    val price: Double,
    val cabinClass: String,
    val departureAirportCode: String,
    val destinationAirportCode: String,
    val departureDate: LocalDateTime,
    val arrivalDate: LocalDateTime
)

