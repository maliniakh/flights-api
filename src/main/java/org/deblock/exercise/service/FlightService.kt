package org.deblock.exercise.service

import kotlinx.datetime.LocalDate
import org.deblock.exercise.client.ApiClient
import org.deblock.exercise.model.FlightResponse
import org.springframework.stereotype.Service

import kotlinx.coroutines.*

@Service
class FlightService(
    private val apiClients: List<ApiClient>
) {
    fun searchFlights(
        origin: String,
        destination: String,
        departureDate: LocalDate,
        returnDate: LocalDate,
        numberOfPassengers: Int
    ): List<FlightResponse> = runBlocking {
        // connection pooling should be used in a real life application (restClient configuration)
        // coroutine dispatcher with a thread pool should be used too
        apiClients
            .map { client ->
                async(Dispatchers.IO) {
                    client.queryFlights(origin, destination, departureDate, returnDate, numberOfPassengers)
                }
            }
            .awaitAll()
            .flatten()
            .sortedBy { it.fare }
    }
}