package com.unab.registroqr.data

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

/**
 * Clase de datos para representar un horario individual
 */
data class ScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val day: DayOfWeek,
    val time: LocalTime
)
