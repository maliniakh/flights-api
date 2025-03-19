package org.deblock.exercise.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.UTC): LocalDateTime {
    return Clock.System.now().toLocalDateTime(timeZone)
}