package com.daon.fido.sdk.sample.kt.service.rpsa

import android.os.Bundle
import com.daon.sdk.crypto.log.LogUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * @suppress
 */
class HTTP(private val params: Bundle) {
    sealed class HttpResponse
    data class Success(val payload: String?, val httpStatusCode: Int) : HttpResponse()
    data class Error(val code: Int, val message: String) : HttpResponse()

    fun get(relativeUrl: String, sessionId: String?): HttpResponse {
        try {
            val urlConnection = createConnection(relativeUrl, GET_METHOD, false, sessionId)
            val httpResult = urlConnection.responseCode
            val response: String =
                if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                    readStream(urlConnection.inputStream)
                } else {
                    readStream(urlConnection.errorStream)
                }
            return Success(response, httpResult)

        } catch (e: MalformedURLException) {
            return Error(-1, "Unable to connect to the server - likely a programming error")
        } catch (e: IOException) {
            return Error(
                -2,
                "Unable to connect to the server.\n" + "Check the internet connection and server URL."
            )
        }
    }


    fun post(relativeUrl: String, payload: String, sessionId: String?): HttpResponse {
        try {
            val urlConnection = createConnection(relativeUrl, POST_METHOD, true, sessionId)

            OutputStreamWriter(urlConnection.outputStream).use {
                it.write(payload)
            }

            val httpResult = urlConnection.responseCode
            val response: String =
                if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                    readStream(urlConnection.inputStream)
                } else {
                    readStream(urlConnection.errorStream)
                }
            return Success(response, httpResult)
        } catch (e: MalformedURLException) {
            return Error(-1, "Unable to connect to the server - likely a programming error")
        } catch (e: IOException) {
            return Error(
                -2,
                "Unable to connect to the server.\n" + "Check the internet connection and server URL."
            )
        }

    }

    private fun createConnection(
        relativeUrl: String, method: String, output: Boolean, sessionId: String?
    ): HttpURLConnection {
        val url = URL(getAbsoluteUrl(relativeUrl))
        LogUtils.logVerbose(null, "IXUAF_KT", "createConnection URL :$url")
        val httpURLConnection = (url.openConnection() as HttpURLConnection).apply {
            doOutput = output
            requestMethod = method
            useCaches = false
            connectTimeout = CONNECTION_TIMEOUT
            readTimeout = READ_TIMEOUT
            setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE)
            setRequestProperty(SESSION_IDENTIFIER_HEADER, sessionId)
        }
        return httpURLConnection
    }

    private fun getAbsoluteUrl(relativeUrl: String): String {
        return getBaseUrl() + relativeUrl
    }

    private fun getBaseUrl(): String {
        return params.getString("server_url").toString()
    }

    private fun readStream(stream: InputStream): String {

        val lines = stream.use {
            it.bufferedReader().readLines()
        }

        return lines.joinToString(" ")
    }

    fun deleteResource(resource: String, resourceId: String, withOutput: Boolean, sessionId: String?): HttpResponse {
        val relativeUrl = "$resource/$resourceId"
        try {
            val urlConnection = createConnection(relativeUrl, DELETE_METHOD, false, sessionId)
            val httpResult = urlConnection.responseCode
            if (httpResult == HttpURLConnection.HTTP_OK) {
                if (withOutput) {
                    val response = readStream(urlConnection.inputStream)
                    return Success(response, httpResult)
                }
                return Success(null, httpResult)
            } else {
                val response = readStream(urlConnection.errorStream)
                return Success(response, httpResult)
            }
        } catch (e: MalformedURLException) {
            return Error(-1, "Unable to connect to the server - likely a programming error")
        } catch (e: IOException) {
            return Error(
                -2,
                "Unable to connect to the server.\n" + "Check the internet connection and server URL."
            )
        }
    }

    companion object{
        const val CONNECTION_TIMEOUT = 20000
        const val READ_TIMEOUT = 20000
        const val CONTENT_TYPE_HEADER = "Content-Type"
        const val CONTENT_TYPE = "application/json"
        const val SESSION_IDENTIFIER_HEADER = "Session-Id"
        const val POST_METHOD = "POST"
        const val GET_METHOD = "GET"
        const val DELETE_METHOD = "DELETE"
    }
}