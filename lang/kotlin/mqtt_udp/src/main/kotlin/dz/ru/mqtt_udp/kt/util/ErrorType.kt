package dz.ru.mqtt_udp.kt.util

enum class ErrorType {
    IO,
    Timeout,
    Protocol,
    Unexpected,
    Invalid			// Invalid parameter
}