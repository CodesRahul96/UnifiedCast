package com.codesrahul.unifiedcast.hardware

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log

class IrRemoteManager(context: Context) {

    private val irManager: ConsumerIrManager? = 
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    val hasIrEmitter: Boolean
        get() = irManager?.hasIrEmitter() == true

    /**
     * Transmit standard NEC 38kHz IR command for keycode
     */
    fun transmitKey(keyCode: Int) {
        if (!hasIrEmitter || irManager == null) {
            Log.d("IrRemoteManager", "No hardware IR Emitter on device")
            return
        }

        try {
            val carrierFrequency = 38000
            val pattern = generateNecPattern(keyCode)
            irManager.transmit(carrierFrequency, pattern)
            Log.d("IrRemoteManager", "Transmitted IR signal for keycode: $keyCode at ${carrierFrequency}Hz")
        } catch (e: Exception) {
            Log.e("IrRemoteManager", "Error transmitting IR signal", e)
        }
    }

    /**
     * Generates a standard NEC protocol IR timing pattern (microsecond pulses)
     */
    private fun generateNecPattern(keyCode: Int): IntArray {
        // Standard NEC Leader Pulse: 9000us mark, 4500us space
        val patternList = mutableListOf(9000, 4500)

        // Standard TV Address 0x00FF
        val address = 0x00FF
        // Map Android KeyEvent keycode to 8-bit IR command code
        val command = when (keyCode) {
            26 -> 0x12 // KEYCODE_POWER
            24 -> 0x1A // KEYCODE_VOLUME_UP
            25 -> 0x1E // KEYCODE_VOLUME_DOWN
            164 -> 0x0F // KEYCODE_VOLUME_MUTE
            19 -> 0x02 // KEYCODE_DPAD_UP
            20 -> 0x03 // KEYCODE_DPAD_DOWN
            21 -> 0x04 // KEYCODE_DPAD_LEFT
            22 -> 0x05 // KEYCODE_DPAD_RIGHT
            23, 66 -> 0x01 // KEYCODE_DPAD_CENTER / ENTER
            4 -> 0x08 // KEYCODE_BACK
            3 -> 0x09 // KEYCODE_HOME
            82 -> 0x0D // KEYCODE_MENU
            else -> 0x00
        }

        // Combine Address, Address Bar, Command, Command Bar (32 bits)
        val data = (address and 0xFFFF) or (command shl 16) or ((command.inv() and 0xFF) shl 24)

        // Transmit 32 bits using NEC bit timings (0 = 560us mark, 560us space; 1 = 560us mark, 1690us space)
        for (i in 0 until 32) {
            val bit = (data shr i) and 1
            patternList.add(560)
            patternList.add(if (bit == 1) 1690 else 560)
        }

        // Stop bit
        patternList.add(560)

        return patternList.toIntArray()
    }
}
