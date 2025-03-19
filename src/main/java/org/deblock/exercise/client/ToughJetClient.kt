package org.deblock.exercise.client

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.deblock.exercise.model.FlightResponse
import org.deblock.exercise.model.SupplierEnum
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class ToughJetClient(restTemplate: RestTemplate) : ApiClient(restTemplate) {
    override val baseUrl: String = "https://toughjet.com"

    override fun buildRequestUri(
        origin: String,
        destination: String,
        departureDate: LocalDate,
        returnDate: LocalDate,
        numberOfPassengers: Int
    ): String {
        return "?from=$origin,to=$destination,outboundDate=$departureDate,inboundDate=$returnDate,numberOfAdults=$numberOfPassengers"
    }

    override fun translateResponse(response: ResponseEntity<String>): List<FlightResponse> {
        return Json.decodeFromString<List<ToughJetResponse>>(response.body!!)
            .map { FlightResponse(
                airline = it.carrier, supplier = SupplierEnum.TOUGH_JET.prettyName,
                fare = BigDecimal
                    .valueOf((it.basePrice + it.tax) * (1 - it.discount / 100))
                    .setScale(2, RoundingMode.HALF_UP),
                departureAirportCode = it.departureAirportName, destinationAirportCode = it.arrivalAirportName,
                departureDate = it.outboundDateTime, arrivalDate = it.inboundDateTime)
            }
    }
}

@Serializable
private data class ToughJetResponse(
    val carrier: String,
    val basePrice: Double,
    val tax: Double,
    val discount: Double,
    val departureAirportName: String,
    val arrivalAirportName: String,
    val outboundDateTime: LocalDateTime,
    val inboundDateTime: LocalDateTime
)