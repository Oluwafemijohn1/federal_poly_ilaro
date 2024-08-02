package com.fpi.biometricsystem.data

data class Message(
    val messageType: MessageType,
    val message: String = ""
)

enum class MessageType {
    ERROR,
    SUCCESS,
}