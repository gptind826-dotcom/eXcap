package com.excap.capture

import android.util.Log
import java.nio.charset.Charset

/**
 * HTTP Inspector
 * Parses and inspects HTTP/HTTPS request and response data.
 * Provides request line parsing, header extraction, and body analysis.
 *
 * Built by eXU CODER
 */
class HttpInspector {

    companion object {
        private const val TAG = "HttpInspector"

        // Common HTTP methods
        val HTTP_METHODS = setOf("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH", "CONNECT", "TRACE")

        // Common HTTP headers
        val COMMON_REQUEST_HEADERS = setOf(
            "Host", "User-Agent", "Accept", "Accept-Language", "Accept-Encoding",
            "Accept-Charset", "Content-Type", "Content-Length", "Connection",
            "Cookie", "Authorization", "Referer", "Origin", "X-Requested-With"
        )

        val COMMON_RESPONSE_HEADERS = setOf(
            "Content-Type", "Content-Length", "Content-Encoding", "Transfer-Encoding",
            "Set-Cookie", "Location", "Cache-Control", "Expires", "Last-Modified",
            "ETag", "Server", "X-Powered-By", "Access-Control-Allow-Origin",
            "Strict-Transport-Security", "X-Frame-Options", "X-Content-Type-Options"
        )
    }

    /**
     * Parse raw HTTP data into structured HTTP message
     */
    fun parseHttpData(data: ByteArray): HttpMessage? {
        return try {
            val text = String(data, Charset.forName("UTF-8"))
            
            if (isHttpRequest(text)) {
                parseHttpRequest(text, data)
            } else if (isHttpResponse(text)) {
                parseHttpResponse(text, data)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse HTTP data", e)
            null
        }
    }

    /**
     * Check if data is HTTP request
     */
    fun isHttpRequest(data: String): Boolean {
        val firstLine = data.lineSequence().firstOrNull()?.trim() ?: return false
        return HTTP_METHODS.any { method ->
            firstLine.startsWith("$method ")
        }
    }

    /**
     * Check if data is HTTP response
     */
    fun isHttpResponse(data: String): Boolean {
        val firstLine = data.lineSequence().firstOrNull()?.trim() ?: return false
        return firstLine.startsWith("HTTP/1.0") || firstLine.startsWith("HTTP/1.1") || firstLine.startsWith("HTTP/2")
    }

    private fun parseHttpRequest(text: String, rawData: ByteArray): HttpMessage {
        val lines = text.lines()
        val requestLine = lines.firstOrNull() ?: ""
        
        // Parse request line: METHOD URL HTTP/VERSION
        val requestParts = requestLine.split(" ", limit = 3)
        val method = requestParts.getOrElse(0) { "" }
        val url = requestParts.getOrElse(1) { "" }
        val version = requestParts.getOrElse(2) { "" }

        // Parse headers
        val headers = parseHeaders(lines.drop(1))

        // Parse body
        val body = extractBody(text)

        return HttpMessage(
            type = HttpMessageType.REQUEST,
            method = method,
            url = url,
            version = version,
            statusCode = 0,
            statusText = "",
            headers = headers,
            body = body,
            rawData = rawData,
            contentType = headers["Content-Type"] ?: "",
            host = headers["Host"] ?: ""
        )
    }

    private fun parseHttpResponse(text: String, rawData: ByteArray): HttpMessage {
        val lines = text.lines()
        val statusLine = lines.firstOrNull() ?: ""

        // Parse status line: HTTP/VERSION STATUS_CODE STATUS_TEXT
        val statusParts = statusLine.split(" ", limit = 3)
        val version = statusParts.getOrElse(0) { "" }
        val statusCode = statusParts.getOrElse(1) { "0" }.toIntOrNull() ?: 0
        val statusText = statusParts.getOrElse(2) { "" }

        // Parse headers
        val headers = parseHeaders(lines.drop(1))

        // Parse body
        val body = extractBody(text)

        return HttpMessage(
            type = HttpMessageType.RESPONSE,
            method = "",
            url = "",
            version = version,
            statusCode = statusCode,
            statusText = statusText,
            headers = headers,
            body = body,
            rawData = rawData,
            contentType = headers["Content-Type"] ?: "",
            host = ""
        )
    }

    private fun parseHeaders(lines: List<String>): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        for (line in lines) {
            if (line.isBlank()) break
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val name = line.substring(0, colonIndex).trim()
                val value = line.substring(colonIndex + 1).trim()
                headers[name] = value
            }
        }
        return headers
    }

    private fun extractBody(text: String): String {
        val headerEndIndex = text.indexOf("\r\n\r\n")
        return if (headerEndIndex >= 0) {
            text.substring(headerEndIndex + 4)
        } else {
            val altEndIndex = text.indexOf("\n\n")
            if (altEndIndex >= 0) {
                text.substring(altEndIndex + 2)
            } else {
                ""
            }
        }
    }

    /**
     * Format HTTP message for display with syntax highlighting
     */
    fun formatForDisplay(message: HttpMessage): String {
        val sb = StringBuilder()

        if (message.type == HttpMessageType.REQUEST) {
            sb.appendLine("${message.method} ${message.url} ${message.version}")
        } else {
            sb.appendLine("${message.version} ${message.statusCode} ${message.statusText}")
        }

        message.headers.forEach { (name, value) ->
            sb.appendLine("$name: $value")
        }

        if (message.body.isNotEmpty()) {
            sb.appendLine()
            // Truncate body if too long
            val maxBodyLength = 10000
            if (message.body.length > maxBodyLength) {
                sb.append(message.body.substring(0, maxBodyLength))
                sb.appendLine("\n... (${message.body.length - maxBodyLength} more bytes)")
            } else {
                sb.append(message.body)
            }
        }

        return sb.toString()
    }

    /**
     * Extract URL from HTTP request
     */
    fun extractUrl(data: ByteArray): String? {
        val text = String(data, Charset.forName("UTF-8"))
        val lines = text.lines()
        val requestLine = lines.firstOrNull() ?: return null
        val parts = requestLine.split(" ", limit = 3)
        return if (parts.size >= 2) parts[1] else null
    }

    /**
     * Extract Host header value
     */
    fun extractHost(data: ByteArray): String? {
        val text = String(data, Charset.forName("UTF-8"))
        val lines = text.lines()
        for (line in lines) {
            if (line.startsWith("Host:", ignoreCase = true)) {
                return line.substringAfter(":").trim()
            }
        }
        return null
    }

    /**
     * Check if response is chunked transfer encoding
     */
    fun isChunked(message: HttpMessage): Boolean {
        return message.headers["Transfer-Encoding"]?.contains("chunked", ignoreCase = true) == true
    }

    /**
     * Check if content is compressed
     */
    fun isCompressed(message: HttpMessage): Boolean {
        val encoding = message.headers["Content-Encoding"] ?: ""
        return encoding.isNotEmpty() && encoding != "identity"
    }
}

/**
 * HTTP Message data class
 */
data class HttpMessage(
    val type: HttpMessageType,
    val method: String,
    val url: String,
    val version: String,
    val statusCode: Int,
    val statusText: String,
    val headers: Map<String, String>,
    val body: String,
    val rawData: ByteArray,
    val contentType: String,
    val host: String
) {
    fun isSuccess(): Boolean = statusCode in 200..299
    fun isRedirect(): Boolean = statusCode in 300..399
    fun isClientError(): Boolean = statusCode in 400..499
    fun isServerError(): Boolean = statusCode in 500..599

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HttpMessage
        return type == other.type &&
                method == other.method &&
                url == other.url &&
                statusCode == other.statusCode &&
                headers == other.headers
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + statusCode
        result = 31 * result + headers.hashCode()
        return result
    }
}

enum class HttpMessageType {
    REQUEST,
    RESPONSE
}
