package org.deblock.exercise.client

import kotlinx.datetime.LocalDate
import org.deblock.exercise.model.FlightResponse
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

abstract class ApiClient(
    private val restTemplate: RestTemplate
) {

    abstract val baseUrl: String

    fun queryFlights(
        origin: String,
        destination: String,
        departureDate: LocalDate,
        returnDate: LocalDate,
        numberOfPassengers: Int
    ) : List<FlightResponse> {
        val url = "$baseUrl/${buildRequestUri(origin, destination, departureDate, returnDate, numberOfPassengers)}"
        val response = restTemplate.exchange(
            url, HttpMethod.GET, null, String::class.java
        )
        return translateResponse(response)
    }

    protected abstract fun buildRequestUri(
        origin: String,
        destination: String,
        departureDate: LocalDate,
        returnDate: LocalDate,
        numberOfPassengers: Int
    ): String

    // alternatively translateResponse could be generic and implemented here, subclasses would
    // implement single object translation, but I think that would be too rigid of an abstraction
    protected abstract fun translateResponse(response: ResponseEntity<String>): List<FlightResponse>

}