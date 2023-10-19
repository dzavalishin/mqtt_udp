package dz.ru.mqtt_udp.kt.util

class MqttProtocolException : Exception {

    constructor(message: String) : super(message)  { }
    constructor(message: String, cause: Throwable) :  super(message, cause) {}

}