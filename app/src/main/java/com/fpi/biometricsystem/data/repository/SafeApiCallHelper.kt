package com.fpi.biometricsystem.data.repository

import com.fpi.biometricsystem.data.GenericError
import com.fpi.biometricsystem.data.SimpleResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): SimpleResponse<T> {
    return withContext(Dispatchers.IO) {
        try {
            val result = apiCall.invoke()
            if (result.isSuccessful) {
                SimpleResponse.success(result)
            } else {
                val error = result.errorBody()?.string()
                val errorMessage = Gson().fromJson(error, GenericError::class.java)
                SimpleResponse.failure(
                    data = null,
                    exception = GenericError(message = errorMessage.message)
                )
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    val error = GenericError("Internet connection error")
                    SimpleResponse.failure(data = null, error)
                }
                is HttpException -> {
                    val code = e.code()
                    val errorResponse = convertErrorBody(e)
                    SimpleResponse.failure(null, GenericError("$code: $errorResponse"))
                }

                else -> {
                    val error = GenericError(e.cause?.message.orEmpty())
                    SimpleResponse.failure(null, error)
                }
            }
        }
    }
}

private fun convertErrorBody(throwable: HttpException): GenericError? {
    return try {
        throwable.response()?.errorBody()?.source()?.let {
            val err = Gson().fromJson(it.buffer.toString(), GenericError::class.java)
            println(err)
            return err
        }
    } catch (exception: Exception) {
        null
    }
}