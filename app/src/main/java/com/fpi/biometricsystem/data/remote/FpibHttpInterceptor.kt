package com.fpi.biometricsystem.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject

class FpibHttpInterceptor : Interceptor {
    @Throws(Exception::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        try {

            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            val newResponse = response.newBuilder()
            val contentType = response.header("Content-Type", "application/json")
            newResponse.body(
                responseBody.toResponseBody(contentType!!.toMediaTypeOrNull())
            )

            response = newResponse.build()

            return response
        } catch (e: Exception) {
            return response
        }
    }
}