package com.unab.registroqr.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para la validación de URLs de QR UNAB
 */
class QRScannerValidationTest {

    @Test
    fun `URL valida con prefijo correcto debe retornar true`() {
        val url = "https://registroasistenciaqr.unab.cl/"
        assertTrue(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL valida con parametros debe retornar true`() {
        val url = "https://registroasistenciaqr.unab.cl/?validate=U2FsdGVkX1+sjm7UJLrU/met9PxQH+JlAWQRlXJTV1Fjjrpmi9Ovjc0G6jl5W3JVDRyecuQx26Ek9oPL4zJ1lnuK2UjroHo7nia+lAPpf9BZ9BTgCHGQCYdkcT+FOL+LGmKEIbXM6UqiD25SRWLL1A==&iv=/CIAsrjmG94KNUEoD7at4A=="
        assertTrue(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL valida con ruta adicional debe retornar true`() {
        val url = "https://registroasistenciaqr.unab.cl/sala/101"
        assertTrue(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL con HTTP en lugar de HTTPS debe retornar false`() {
        val url = "http://registroasistenciaqr.unab.cl/"
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL con dominio com en lugar de cl debe retornar false`() {
        val url = "https://registroasistenciaqr.unab.com/"
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL con www debe retornar false`() {
        val url = "https://www.registroasistenciaqr.unab.cl/"
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL con mayusculas debe retornar false`() {
        val url = "https://REGISTROASISTENCIAQR.UNAB.CL/"
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL de Google debe retornar false`() {
        val url = "https://google.com"
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL con subdominio diferente debe retornar false`() {
        val url = "https://asistencia.unab.cl/"
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL vacia debe retornar false`() {
        val url = ""
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `URL con espacios debe retornar false`() {
        val url = "https://registroasistenciaqr.unab.cl/ "
        assertFalse(QRScanner.isValidUNABQR(url))
    }

    @Test
    fun `Prefijo exacto debe retornar el valor correcto`() {
        assertEquals("https://registroasistenciaqr.unab.cl/", QRScanner.VALID_QR_PREFIX)
    }
}
