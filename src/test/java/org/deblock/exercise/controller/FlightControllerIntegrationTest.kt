package org.deblock.exercise.controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.deblock.exercise.TestConfig
import org.deblock.exercise.model.FlightResponse
import org.deblock.exercise.model.SupplierEnum
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
@ActiveProfiles("test") //
class FlightControllerIntegrationTest {


    private var wiremockPort: Int = 9090

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setupWireMock() {
        // having a random port would've been more ideal, but I struggled too much with getting it configured
        wireMockServer = WireMockServer(wiremockPort)
        wireMockServer.start()
        configureFor("localhost", wiremockPort)

        wireMockServer.stubFor(
            get(urlMatching("/crazyair/flights.*"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                [
                                    { 
                                        "airline": "Qatar", "price": 200.0, 
                                        "departureAirportCode": "LHR", "destinationAirportCode": "AMS",
                                        "cabinClass": "B", 
                                        "departureDate": "2024-02-14T23:00:00", "arrivalDate": "2024-02-14T23:00:00" 
                                    },
                                    { 
                                        "airline": "SwissAir", "price": 150.0, 
                                        "departureAirportCode": "LHR", "destinationAirportCode": "AMS",
                                        "cabinClass": "E", 
                                        "departureDate": "2024-02-14T23:00:00", "arrivalDate": "2024-02-14T23:00:00" 
                                    }
                                    
                                ]
                """.trimIndent()
                        )
                )
        )

        wireMockServer.stubFor(
            get(urlMatching("/toughjet/flights.*"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        // price: 160
                        .withBody(
                            """
                                [
                                    { 
                                        "carrier": "WizzAir", "basePrice": 150.0, "tax": 50.0, "discount": 20,
                                        "departureAirportName": "LHR", "arrivalAirportName": "AMS" 
                                        "outboundDateTime": "2024-02-14T23:00:00", "inboundDateTime": "2024-02-16T23:00:00"
                                    }
                                ]
                """.trimIndent()
                        )
                )
        )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `should return aggregated flight results sorted by fare`() {
        val origin = "LHR"
        val destination = "AMS"
        val departureDate = LocalDate.now().plusDays(7).toString()
        val returnDate = LocalDate.now().plusDays(14).toString()
        val numberOfPassengers = 1

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/flights?origin=$origin&destination=$destination&departureDate=$departureDate&returnDate=$returnDate&numberOfPassengers=$numberOfPassengers",
            Array<FlightResponse>::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(response.body).isNotEmpty
        assertThat(body).hasSize(3)
        assertThat(body[0].fare).isEqualTo(BigDecimal(150).setScale(2))
        assertThat(body[0].supplier).isEqualTo(SupplierEnum.CRAZY_AIR.prettyName)
        assertThat(body[0].airline).isEqualTo("SwissAir")
        assertThat(body[1].fare).isEqualTo(BigDecimal(160).setScale(2))
        assertThat(body[1].airline).isEqualTo("WizzAir")
        assertThat(body[1].supplier).isEqualTo(SupplierEnum.TOUGH_JET.prettyName)
        assertThat(body[2].fare).isEqualTo(BigDecimal(200).setScale(2))
        assertThat(body[2].airline).isEqualTo("Qatar")
        assertThat(body[2].supplier).isEqualTo(SupplierEnum.CRAZY_AIR.prettyName)
    }

    @Test
    fun `return BAD_REQUEST when numberOfPassengers is zero`() {
        val origin = "LHR"
        val destination = "AMS"
        val departureDate = LocalDate.now().plusDays(7).toString()
        val returnDate = LocalDate.now().plusDays(14).toString()
        val numberOfPassengers = 0

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/flights?origin=$origin&destination=$destination&departureDate=$departureDate&returnDate=$returnDate&numberOfPassengers=$numberOfPassengers",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).contains(" must be greater than or equal to 1")
    }
}
