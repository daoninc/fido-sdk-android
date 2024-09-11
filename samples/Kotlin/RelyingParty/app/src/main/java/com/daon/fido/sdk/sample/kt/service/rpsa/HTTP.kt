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

    private var sessionId: String? = null

    fun setSessionId(id: String?) {
        sessionId = id
    }

    fun get(relativeUrl: String): HttpResponse {
        try {
            val urlConnection = createConnection(relativeUrl, GET_METHOD, false)
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


    fun post(relativeUrl: String, payload: String): HttpResponse {
        try {
            val urlConnection = createConnection(relativeUrl, POST_METHOD, true)
            val outputStreamWriter = OutputStreamWriter(urlConnection.outputStream)
            outputStreamWriter.write(payload)
            outputStreamWriter.close()

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
        relativeUrl: String, method: String, output: Boolean
    ): HttpURLConnection {
        val url = URL(getAbsoluteUrl(relativeUrl))
        LogUtils.logVerbose(null, "IXUAF_KT", "createConnection URL :$url")
        val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        httpURLConnection.doOutput = output
        httpURLConnection.requestMethod = method
        httpURLConnection.useCaches = false
        httpURLConnection.connectTimeout = CONNECTION_TIMEOUT
        httpURLConnection.readTimeout = READ_TIMEOUT
        httpURLConnection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE)
        //ToDO - check for SessionId null
        httpURLConnection.setRequestProperty(SESSION_IDENTIFIER_HEADER, sessionId)
        httpURLConnection.connect()
        return httpURLConnection
    }

    private fun getAbsoluteUrl(relativeUrl: String): String {
        return getBaseUrl() + relativeUrl
    }

    private fun getBaseUrl(): String {
        return params.getString("server_url").toString()
    }

    private fun readStream(stream: InputStream): String {
        val sb = StringBuilder()
        val br = BufferedReader(InputStreamReader(stream, "utf-8"))
        var line: String?
        while (run {
                line = br.readLine()
                line
            } != null) {
            sb.append(line)
        }
        br.close()
        return sb.toString()
    }

    fun deleteResource(resource: String, resourceId: String, withOutput: Boolean): HttpResponse {
        val relativeUrl = "$resource/$resourceId"
        try {
            val urlConnection = createConnection(relativeUrl, DELETE_METHOD, false)
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