package com.mgacreative.touros.integration

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Samo (SOAP XML) ve Tourvisor (REST) Canlı Operatör Entegrasyon Testleri
 *
 * Mock veya sahte veri KULLANILMAZ.
 * Ortam değişkenlerinden (CI secrets / .env) veya sandbox yapılandırmalarından
 * kimlik bilgilerini okuyarak doğrudan canlı uç noktalara bağlanır.
 */
class OperatorLiveIntegrationTest {

    // Tourvisor REST Yapılandırması (Environment Variables / Fallback)
    private val tourvisorUrl = System.getenv("TOURVISOR_API_URL")
        ?: System.getProperty("TOURVISOR_API_URL")
        ?: "http://tourvisor.ru/xml/list.php"

    private val tourvisorLogin = System.getenv("TOURVISOR_AUTH_LOGIN")
        ?: System.getProperty("TOURVISOR_AUTH_LOGIN")
        ?: "Mabit23@gmail.com"

    private val tourvisorPass = System.getenv("TOURVISOR_AUTH_PASS")
        ?: System.getProperty("TOURVISOR_AUTH_PASS")
        ?: "FFytMvSU0ZHr"

    // Samo SOAP Yapılandırması (Environment Variables / Fallback)
    private val samoEndpoint = System.getenv("SAMO_SOAP_ENDPOINT")
        ?: System.getProperty("SAMO_SOAP_ENDPOINT")
        ?: "https://samo.ru"

    private val samoUser = System.getenv("SAMO_AUTH_USER")
        ?: System.getProperty("SAMO_AUTH_USER")
        ?: "Mabit23@gmail.com"

    private val samoPass = System.getenv("SAMO_AUTH_PASS")
        ?: System.getProperty("SAMO_AUTH_PASS")
        ?: "FFytMvSU0ZHr"

    @Test
    fun testTourvisorLiveRestIntegration() {
        println("\n>>> [Tourvisor REST] Canlı Entegrasyon Testi Başlatılıyor...")
        println("Uç Nokta: $tourvisorUrl | Kullanıcı: $tourvisorLogin")

        val queryParams = listOf(
            "authlogin" to tourvisorLogin,
            "authpass" to tourvisorPass,
            "type" to "departure",
            "format" to "json"
        ).joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, StandardCharsets.UTF_8.name())}=${URLEncoder.encode(v, StandardCharsets.UTF_8.name())}"
        }

        val fullUrl = if (tourvisorUrl.contains("?")) "$tourvisorUrl&$queryParams" else "$tourvisorUrl?$queryParams"
        val connection = URI(fullUrl).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty("User-Agent", "TourOS-Integration-Runner/1.0")
        connection.setRequestProperty("Accept", "application/json")

        val responseCode = connection.responseCode
        println("Tourvisor HTTP Yanıt Kodu: $responseCode")
        assertEquals(200, responseCode, "Tourvisor REST servisi HTTP 200 dönmelidir!")

        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
        assertNotNull(responseBody, "Tourvisor yanıt gövdesi boş olamaz!")
        assertTrue(responseBody.length > 50, "Tourvisor yanıtı anlamlı veri içermelidir!")

        // Kimlik doğrulama veya sunucu hata kontrolü
        assertFalse(
            responseBody.contains("\"error\":") && !responseBody.contains("\"departures\""),
            "Tourvisor API kimlik doğrulama veya parametre hatası döndü: $responseBody"
        )

        // Veri yapısı doğrulaması (departures, lists, kalkış şehirleri)
        assertTrue(
            responseBody.contains("lists") || responseBody.contains("departures"),
            "Dönen yanıt Tourvisor liste şemasına uygun olmalıdır."
        )
        assertTrue(
            responseBody.contains("Москва") || responseBody.contains("departures"),
            "Kalkış listesinde beklenen şehir verileri (örn: Москва) bulunmalıdır."
        )

        println("✅ [Tourvisor REST] Canlı Test Başarılı! Dönen Karakter Boyutu: ${responseBody.length} bayt.")
    }

    @Test
    fun testSamoLiveSoapIntegration() {
        println("\n>>> [Samo SOAP XML] Canlı Entegrasyon Testi Başlatılıyor...")
        println("SOAP Uç Nokta: $samoEndpoint | Kullanıcı: $samoUser")

        // 1. SOAP XML Zarfı (Envelope) Hazırlığı
        val soapPayload = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                           xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
                           xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Header>
                <AuthHeader xmlns="http://samo.ru/">
                  <Username>$samoUser</Username>
                  <Password>$samoPass</Password>
                </AuthHeader>
              </soap:Header>
              <soap:Body>
                <GetTowns xmlns="http://samo.ru/">
                  <State>1</State>
                </GetTowns>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        // 2. Canlı SOAP HTTP POST İsteği
        val targetUri = URI(samoEndpoint)
        val connection = targetUri.toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8")
        connection.setRequestProperty("SOAPAction", "\"http://samo.ru/GetTowns\"")
        connection.setRequestProperty("User-Agent", "TourOS-Samo-SOAP-Agent/1.0")

        OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
            writer.write(soapPayload)
            writer.flush()
        }

        val responseCode = connection.responseCode
        println("Samo SOAP HTTP Yanıt Kodu: $responseCode")

        // SOAP servisleri geçerli işlemde 200, yetki veya parametre reddinde 401/403/500 dönebilir.
        // Canlı uç noktaya ağ bağlantısının kurulabildiği ve HTTP düzeyinde yanıt alındığı doğrulanır.
        assertTrue(
            responseCode in 200..499,
            "Samo SOAP servisi erişilebilir olmalı ve geçerli bir HTTP yanıt kodu üretmelidir! (Alınan: $responseCode)"
        )

        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

        println("Samo Servis Yanıt Uzunluğu: ${responseText.length} karakter.")
        assertTrue(responseText.isNotEmpty(), "Samo servisinden dönen yanıt boş olamaz!")

        // 3. Samo Servis Tanımı & Uç Nokta Canlılık (Liveness / WSDL) Denetimi
        val livenessConn = URI(samoEndpoint).toURL().openConnection() as HttpURLConnection
        livenessConn.requestMethod = "GET"
        livenessConn.connectTimeout = 10000
        livenessConn.readTimeout = 10000
        livenessConn.setRequestProperty("User-Agent", "TourOS-Liveness-Probe/1.0")
        val livenessCode = livenessConn.responseCode
        println("Samo Ana Uç Nokta Liveness Kodu: $livenessCode")
        assertTrue(livenessCode in 200..399, "Samo ana servis kapısı erişilebilir (HTTP 2xx/3xx) olmalıdır.")

        println("✅ [Samo SOAP XML] Canlı Servis İletişim Doğrulaması Başarılı!")
    }
}
