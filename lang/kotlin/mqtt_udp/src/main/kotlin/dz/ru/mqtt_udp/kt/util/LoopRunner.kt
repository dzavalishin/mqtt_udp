package dz.ru.mqtt_udp.kt.util

import dz.ru.mqtt_udp.kt.GlobalErrorHandler
import java.io.IOException
import kotlin.concurrent.Volatile


/**
 * Set up thread, run user's code in a loop. Provide start and stop controls.
 *
 * @param threadName Thread name
 *
 * @author dz
 *
 */

abstract class LoopRunner(val threadName : String) : Sleeper() {


    // ------------------------------------------------------------
    // For user to override
    // ------------------------------------------------------------
    /**
     * To be overridden in subclass. On start do preparations needed.
     *
     * @throws IOException In case of IO error
     *
     * @throws MqttProtocolException In case of MQTT/UDP protocol error
     */
    @Throws(IOException::class, MqttProtocolException::class)
    protected abstract fun onStart()

    /**
     * To be overridden in subclass. Called in loop to do actual work.
     *
     * @throws IOException In case of IO error
     *
     * @throws MqttProtocolException In case of MQTT/UDP protocol error
     */
    @Throws(IOException::class, MqttProtocolException::class)
    protected abstract fun step()

    /**
     * To be overridden in subclass. On stop do cleanup needed.
     *
     * @throws IOException In case of IO error
     *
     * @throws MqttProtocolException In case of MQTT/UDP protocol error
     */
    @Throws(IOException::class, MqttProtocolException::class)
    protected abstract fun onStop()



    // ------------------------------------------------------------
    // Thread
    // ------------------------------------------------------------
    @Volatile
    private var run = false

    fun isRunning(): Boolean {
        return run
    }


    /**
     * Request to start reception loop thread.
     */
    fun requestStart() {
        if (isRunning()) return
        start()
    }

    /**
     * Request to stop reception loop thread.
     */
    fun requestStop() {
        run = false
    }

    /**
     * Worker: start loop thread.
     */
    protected fun start() {
        val target = makeLoopRunnable()
        val t = Thread(target, threadName)
        t.start()
    }


    @Throws(IOException::class, MqttProtocolException::class)
    private fun loop() {
        onStart()
        run = true
        while (run) {
            step()
        }
        onStop()
    }


    private fun makeLoopRunnable(): Runnable {
        return Runnable {
            try {
                loop()
            } catch (e: IOException) {
                GlobalErrorHandler.handleError(ErrorType.IO, e)
            } catch (e: MqttProtocolException) {
                GlobalErrorHandler.handleError(ErrorType.Protocol, e)
            }
        }
    }




}