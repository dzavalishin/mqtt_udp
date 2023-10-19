package dz.ru.mqtt_udp.kt

import dz.ru.mqtt_udp.kt.util.ErrorType
import java.util.function.BiConsumer




object GlobalErrorHandler {
    private var handler: BiConsumer<ErrorType?, String?>? = null

    fun getHandler(): BiConsumer<ErrorType?, String?>? {
        return handler
    }

    fun setHandler(h: BiConsumer<ErrorType?, String?>) {
        handler = h
    }


    fun handleError(type: ErrorType, th: Throwable) {
        handleError(type, th.toString())
    }

    fun handleError(type: ErrorType, description: String) {
        val h = handler;
        if(h != null)
            h.accept(type, description)
        else {
            // TODO logger
            //System.out.println(String.format( "MQTT/UDP %s error: %s", type.toString(), description ));
            System.err.println(java.lang.String.format("MQTT/UDP %s error: %s", type.toString(), description))
        }
    }

}