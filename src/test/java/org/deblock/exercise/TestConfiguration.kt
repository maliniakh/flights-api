package org.deblock.exercise

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.deblock.exercise.client.CrazyAirClient
import org.deblock.exercise.client.ToughJetClient
import org.deblock.exercise.controller.StringToKotlinxLocalDateConverter
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import java.time.format.DateTimeFormatter

@TestConfiguration
class TestConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun localDateConverted(): StringToKotlinxLocalDateConverter {
        return StringToKotlinxLocalDateConverter()
    }

    @Bean
    fun crazyAirClient(restTemplate: RestTemplate): CrazyAirClient {
        return object : CrazyAirClient(restTemplate) {
            override val baseUrl = "http://localhost:9090/crazyair/flights"
        }
    }

    @Bean
    fun toughJetClient(restTemplate: RestTemplate): ToughJetClient {
        return object : ToughJetClient(restTemplate) {
            override val baseUrl = "http://localhost:9090/toughjet/flights"
        }
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val module = SimpleModule()
            .addSerializer(LocalDateTimeSerializer())
            .addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer())

        return jacksonObjectMapper().registerModule(module)
    }
}

class LocalDateTimeSerializer : StdSerializer<LocalDateTime>(LocalDateTime::class.java) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(value: LocalDateTime, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toJavaLocalDateTime().format(formatter))
    }
}

class LocalDateTimeDeserializer : StdDeserializer<LocalDateTime>(LocalDateTime::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        return java.time.LocalDateTime.parse(p.text, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toKotlinLocalDateTime()
    }
}

