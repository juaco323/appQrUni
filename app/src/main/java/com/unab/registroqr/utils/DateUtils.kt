package com.unab.registroqr.utils

import java.time.DayOfWeek

/**
 * Convierte DayOfWeek a español
 */
fun getDayInSpanish(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> "Lunes"
        DayOfWeek.TUESDAY -> "Martes"
        DayOfWeek.WEDNESDAY -> "Miércoles"
        DayOfWeek.THURSDAY -> "Jueves"
        DayOfWeek.FRIDAY -> "Viernes"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
    }
}
