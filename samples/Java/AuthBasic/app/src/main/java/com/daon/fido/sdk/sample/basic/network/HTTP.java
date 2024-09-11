// Copyright (C) 2022 Daon.
//
// Permission to use, copy, modify, and/or distribute this software for any purpose with or without
// fee is hereby granted.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
// SOFTWARE INCLUDING ALL IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
// SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
// DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER
// TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

package com.daon.fido.sdk.sample.basic.network;

import android.content.Context;
import android.util.Log;

import com.daon.fido.sdk.sample.basic.model.Error;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;
import com.daon.fido.sdk.sample.basic.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HTTP {
    private static final String TAG = HTTP.class.getSimpleName();
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int READ_TIMEOUT = 20000;
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE = "application/json";
    private static final String SESSION_IDENTIFIER_HEADER = "Session-Id";

    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";
    private static final String DELETE_METHOD = "DELETE";
    private final GsonBuilder builder;
    private final Context context;

    public static class HttpResponse {
        private final String payload;
        private final int httpStatusCode;

        HttpResponse(String payload, int httpStatusCode) {
            this.payload = payload;
            this.httpStatusCode = httpStatusCode;
        }

        public String getPayload() {
            return this.payload;
        }

        public int getHttpStatusCode() {
            return this.httpStatusCode;
        }
    }

    public HTTP(Context context) {
        builder = new GsonBuilder();
        this.context = context;
    }

    public <T> T get(String resource, String id, Class<T> clazz) {
        return this.get(resource + "/" + id, clazz);
    }

    public <T> T get(String resource, Class<T> clazz) {
        HttpResponse response = get(resource);
        Gson outputGson = builder.create();
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_CREATED || response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "get response:" + response.getPayload());
            return outputGson.fromJson(response.getPayload(), clazz);
        } else {
            Error error;
            try {
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                Log.e(TAG, "Server communications error. HttpResponse code: " + response.getHttpStatusCode());
                error = new Error(-4, "Server communications error. See client ADB logs for more detail.");
            }
            throw new ServerError(error);
        }
    }

    protected HttpResponse get(String relativeUrl) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = createConnection(relativeUrl, GET_METHOD, false);

            int httpResult = urlConnection.getResponseCode();
            Log.d(TAG, "RPSAService get httpResult:" + httpResult);
            String response;
            if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                response = this.readStream(urlConnection.getInputStream());
            } else if (httpResult == HttpURLConnection.HTTP_UNAVAILABLE) {
                throw new IOException("The server connection is unavailable.");
            } else {
                response = this.readStream(urlConnection.getErrorStream());
            }

            return new HttpResponse(response, httpResult);
        } catch (MalformedURLException e) {
            Error error = new Error();
            error.setCode(-1);
            error.setMessage("Unable to connect to the server - likely a programming error");
            throw new CommunicationsException(error);
        } catch (IOException e) {
            Error error = new Error();
            error.setCode(-2);
            error.setMessage("Unable to connect to the server.\nCheck the internet connection and server URL.");
            throw new CommunicationsException(error);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    public <T> T post(String resource, Object object, Class<T> clazz) {

        Gson inputGson = builder.create();
        String payload = inputGson.toJson(object);
        HttpResponse response = this.post(resource, payload);
        Gson outputGson = builder.create();
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_CREATED || response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "post response:" + response.getPayload());
            return outputGson.fromJson(response.getPayload(), clazz);
        } else {
            Error error;
            try {
                Log.d(TAG, "post error response :" + response.getPayload());
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                Log.e("SampleRpApp", "Server communications error. HttpResponse code: " + response.getHttpStatusCode());
                error = new Error(-4, "Server communications error. See client ADB logs for more detail.");
            }
            throw new ServerError(error);
        }
    }

    protected HttpResponse post(String relativeUrl, String payload) {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = this.createConnection(relativeUrl, POST_METHOD, true);
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(payload);
            out.close();

            Log.d(TAG, "**POST**");
            Log.d(TAG, "URL: " + urlConnection.getURL().toString());
            Log.d(TAG, "Payload: " + payload);

            int httpResult = urlConnection.getResponseCode();
            Log.d(TAG, "HTTP result: " + urlConnection.getResponseCode());
            String response;
            if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                response = this.readStream(urlConnection.getInputStream());
            } else {
                response = this.readStream(urlConnection.getErrorStream());
            }

            Log.d(TAG, "Response: " + response);

            return new HttpResponse(response, httpResult);

        } catch (MalformedURLException e) {
            Error error = new Error();
            error.setCode(-1);
            error.setMessage("Unable to connect to the server - likely a programming error");
            throw new CommunicationsException(error);
        } catch (IOException e) {
            Error error = new Error();
            error.setCode(-2);
            error.setMessage("Unable to connect to the server.\nCheck the internet connection and server URL");
            throw new CommunicationsException(error);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    public String deleteResource(String resource, String resourceId, boolean withOutput) {

        HttpResponse response = this.delete(resource, resourceId, withOutput);
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            if (withOutput) {
                return response.getPayload();
            } else {
                return null;
            }
        } else {
            Gson outputGson = builder.create();
            Error error;
            try {
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                Log.e("SampleRpApp", "Server communications error. HttpResponse code: " + response.getHttpStatusCode());
                error = new Error(-4, "Server communications error. See client ADB logs for more detail.");
            }
            throw new ServerError(error);
        }
    }

    protected <T> T deleteResource(String resource, String resourceId, Class<T> clazz) {

        HttpResponse response = this.delete(resource, resourceId, true);
        Gson outputGson = builder.create();
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            return outputGson.fromJson(response.getPayload(), clazz);
        } else {
            Error error;
            try {
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                error = new Error(-4, "Unknown server error");
            }
            throw new ServerError(error);
        }
    }

    protected HttpResponse delete(String relativeUrl, String id, boolean withOutput) {
        return this.delete(relativeUrl + "/" + id, withOutput);
    }

    protected HttpResponse delete(String relativeUrl, boolean withOutput) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = this.createConnection(relativeUrl, DELETE_METHOD, false);
            int httpResult = urlConnection.getResponseCode();
            String response = null;
            if (httpResult == HttpURLConnection.HTTP_OK) {
                if (withOutput) {
                    response = this.readStream(urlConnection.getInputStream());
                }
            } else {
                response = this.readStream(urlConnection.getErrorStream());
            }
            return new HttpResponse(response, httpResult);

        } catch (MalformedURLException e) {

            Error error = new Error();
            error.setCode(-1);
            error.setMessage("Unable to connect to the server - likely a programming error");
            throw new CommunicationsException(error);
        } catch (IOException e) {

            Error error = new Error();
            error.setCode(-2);
            error.setMessage("Unable to connect to the server.\nCheck the internet connection and server URL");
            throw new CommunicationsException(error);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    protected HttpURLConnection createConnection(String relativeUrl, String method, boolean output) throws IOException {
        Log.d(TAG, "createConnection URL:" + getAbsoluteUrl(relativeUrl) + " method:" + method);
        URL url = new URL(getAbsoluteUrl(relativeUrl));
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(output);
        httpURLConnection.setRequestMethod(method);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        httpURLConnection.setReadTimeout(READ_TIMEOUT);
        httpURLConnection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
        // This is required for CreateRegRequest
        Log.d(TAG, "createConnection sessionId :" + RPSAService.sessionId);
        if (RPSAService.sessionId != null)
            httpURLConnection.setRequestProperty(SESSION_IDENTIFIER_HEADER, RPSAService.sessionId);
        httpURLConnection.connect();
        return httpURLConnection;
    }

    protected String readStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    protected String getAbsoluteUrl(String relativeUrl) throws IOException {
        return getBaseUrl() + relativeUrl;
    }

    protected String getBaseUrl() throws IOException {
        return Config.getProperty(Config.SERVER_URL, context);
    }
}
