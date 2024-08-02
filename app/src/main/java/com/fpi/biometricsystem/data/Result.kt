package com.fpi.biometricsystem.data

import retrofit2.Response

data class SimpleResponse<T>(
    val status: Status,
    val data: Response<T>?,
    val exception: GenericError?
) {
    companion object {
        fun <T> success(data: Response<T>): SimpleResponse<T> {
            return if (data.code() == 200) {
                SimpleResponse(status = Status.Success, data, null)
            } else {
                failure(data = data, GenericError(data.message()))
            }
        }

        fun <T> failure(data: Response<T>?, exception: GenericError): SimpleResponse<T> {
            return SimpleResponse(Status.Failure, data, exception)
        }
    }

    sealed class Status {
        object Success : Status()
        object Failure : Status()
    }

    val failed: Boolean
        get() = this.status == Status.Failure
    val isSuccessful: Boolean
        get() = !failed && this.data?.isSuccessful == true
    val body: T
        get() = this.data!!.body()!!
}

