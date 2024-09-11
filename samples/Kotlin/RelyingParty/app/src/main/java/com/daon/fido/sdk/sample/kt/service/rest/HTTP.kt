package com.daon.fido.sdk.sample.kt.service.rest

import android.os.Bundle
import android.util.Base64
import com.daon.sdk.crypto.log.LogUtils
import java.io.*
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * @suppress
 */
class HTTP(private val params: Bundle) {
    sealed class HttpResponse
    data class Success(val payload: String?, val httpStatusCode: Int): HttpResponse()
    data class Error(val code: Int, val message: String): HttpResponse()

    fun post(url: String, payload: String): HttpResponse {
        LogUtils.logVerbose(null, "IXUAF_KT", "HTTP post url - $url")
        try {
            val urlConnection: HttpURLConnection = createConnection(url, POST, true)

            val out = OutputStreamWriter(urlConnection.outputStream)
            out.write(payload)
            out.flush()
            out.close()

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
                -2, "Unable to connect to the server.\n" +
                        "Check the internet connection and server URL."
            )
        }

    }

    fun get(url: String): HttpResponse {
        LogUtils.logVerbose(null, "IXUAF_KT","HTTP::get url $url")
        try {
            val urlConnection: HttpURLConnection = createConnection(url, GET, false)
            LogUtils.logVerbose(null, "IXUAF_KT", "Server URL : ${urlConnection.url}")
            val httpResult = urlConnection.responseCode
            val response: String =
                if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                    readStream(urlConnection.inputStream)
                } else {
                    readStream(urlConnection.errorStream)
                }
            return Success(response, httpResult)
        }catch ( e: MalformedURLException) {
            return Error(-1, "Unable to connect to the server - likely a programming error")
        } catch(e: IOException) {
            return Error(
                -2, "Unable to connect to the server.\n" +
                        "Check the internet connection and server URL."
            )
        }

    }

    private fun createConnection(url: String, method: String, output: Boolean): HttpURLConnection {
        @Suppress("NAME_SHADOWING") val url = URL(getAbsoluteUrl(url))
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.doOutput = output
        urlConnection.requestMethod = method
        urlConnection.useCaches = false
        urlConnection.connectTimeout = CONNECTION_TIMEOUT
        urlConnection.readTimeout = READ_TIMEOUT
        urlConnection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE)
        urlConnection.setRequestProperty(ACCEPT_HEADER, ACCEPT_VALUE)
        urlConnection.setRequestProperty(ACCEPT_ENCODING_HEADER, ACCEPT_ENCODING_VALUE)
        urlConnection.setRequestProperty(CLIENT_TYPE_HEADER, CLIENT_TYPE_VALUE)

        if(url.toString().startsWith("https")) {
            urlConnection.setRequestProperty("Authorization", getBasicAuth())
        }

        return urlConnection
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

    private fun getBasicAuth(): String {
        val username = params.getString("username")
        val password = params.getString("password")
        val userAndPwd: String = username +
                ":" +
                password
        return "Basic "+ Base64.encodeToString(userAndPwd.toByteArray(), Base64.NO_WRAP)
    }

    private fun getAbsoluteUrl(entity: String): String {
        val serverUrl = params.getString("server_url")
        val restPath = params.getString("rest_path")
        return "$serverUrl/$restPath/$entity"
        //return scheme + "://" + url.getHost() + ":" + port + url.getPath() + "/" + RESTPath + "/" + entity
    }

    companion object{
        const val GET = "GET"
        const val POST = "POST"
        const val CONNECTION_TIMEOUT = 20000
        const val READ_TIMEOUT = 20000
        const val CONTENT_TYPE_HEADER = "Content-Type"
        const val CONTENT_TYPE = "application/json; charset=utf-8"
        const val ACCEPT_HEADER = "Accept"
        const val ACCEPT_VALUE = "*/*"
        const val ACCEPT_ENCODING_HEADER = "Accept-Encoding"
        const val ACCEPT_ENCODING_VALUE = "identity"
        const val CLIENT_TYPE_HEADER = "ClientType"
        const val CLIENT_TYPE_VALUE = "id=DemoApp; version=1.0"
    }
}