package com.vogo.vogobletest.BLE

import java.util.*

class BLEConstants {
    companion object {
        private const val TAG = "BLE"

        const val START_RIDE = "startride"
        const val UNLOCK_BIKE = "bikeon"
        const val LOCK_BIKE = "bikeoff"
        const val OPEN_DICKY = "dickyopen"
        const val END_RIDE = "endride"

        const val RESPONSE_INVALID = "-1"
        const val RESPONSE_UNLOCK_BIKE = "1"
        const val RESPONSE_LOCK_BIKE = "3"
        const val RESPONSE_DICKY_OPEN = "2"

        const val RESPONSE_END_RIDE_DICKY_OPEN = "400"
        const val RESPONSE_END_RIDE_SUCCESS = "401"
        const val RESPONSE_END_RIDE_BOTH_ON = "410"
        const val RESPONSE_END_RIDE_IGNITION_ON = "411"

        const val RESPONSE_CONNECTION_FAILED = "6543"

        const val START_BYTE = 36
        const val END_BYTE = 59
        const val COMMAND_BYTE = 99
        const val DATA_BYTE = 98

        const val REQUEST_UNLOCK_BIKE_IC = 65
        const val REQUEST_START_RIDE_IC = 65
        const val REQUEST_OPEN_DICKY_IC = 67
        const val REQUEST_LOCK_BIKE_IC = 69
        const val REQUEST_END_RIDE_IC = 69

        const val SUCCESS_UNLOCK_BIKE_IC = 42
        const val SUCCESS_START_RIDE_IC = 43
        const val SUCCESS_OPEN_DICKY_IC = 44
        const val SUCCESS_LOCK_BIKE_IC = 45
        const val SUCCESS_END_RIDE_IC = 46

        @JvmStatic
        fun getStartRideCommand(): String {
            return String.format(Locale.ENGLISH, "%02X%02X%02X%02X%02X", START_BYTE, COMMAND_BYTE,
                    REQUEST_START_RIDE_IC, SUCCESS_START_RIDE_IC, END_BYTE)
        }

        @JvmStatic
        fun getEndRideCommand(): String {
            return String.format(Locale.ENGLISH, "%02X%02X%02X%02X%02X", START_BYTE, COMMAND_BYTE,
                    REQUEST_END_RIDE_IC, SUCCESS_END_RIDE_IC, END_BYTE)
        }

        @JvmStatic
        fun getUnlockBikeCommand(): String {
            return String.format(Locale.ENGLISH, "%02X%02X%02X%02X%02X", START_BYTE, COMMAND_BYTE,
                    REQUEST_UNLOCK_BIKE_IC, SUCCESS_UNLOCK_BIKE_IC, END_BYTE)
        }

        @JvmStatic
        fun getLockBikeCommand(): String {
            return String.format(Locale.ENGLISH, "%02X%02X%02X%02X%02X", START_BYTE, COMMAND_BYTE,
                    REQUEST_LOCK_BIKE_IC, SUCCESS_LOCK_BIKE_IC, END_BYTE)
        }

        @JvmStatic
        fun getOpenDickyCommand(): String {
            return String.format(Locale.ENGLISH, "%02X%02X%02X%02X%02X", START_BYTE, COMMAND_BYTE,
                    REQUEST_OPEN_DICKY_IC, SUCCESS_OPEN_DICKY_IC, END_BYTE)
        }

        @JvmStatic
        fun verifyStartRideResponse(response: String?): Boolean {
            return try {
                return SUCCESS_START_RIDE_IC.toChar() == response?.toCharArray()?.firstOrNull()
            } catch (exception: Exception) {
                //Timber.tag(TAG).e(exception)
                false
            }
        }

        @JvmStatic
        fun verifyEndRideResponse(response: String?): Boolean {
            return try {
                return SUCCESS_END_RIDE_IC.toChar() == response?.toCharArray()?.firstOrNull()
            } catch (exception: Exception) {
               // Timber.tag(TAG).e(exception)
                false
            }
        }

        @JvmStatic
        fun verifyLockBikeResponse(response: String?): Boolean {
            return try {
                return SUCCESS_LOCK_BIKE_IC.toChar() == response?.toCharArray()?.firstOrNull()
            } catch (exception: Exception) {
                //Timber.tag(TAG).e(exception)
                false
            }
        }

        @JvmStatic
        fun verifyUnlockBikeResponse(response: String?): Boolean {
            return try {
                SUCCESS_UNLOCK_BIKE_IC.toChar() == response?.toCharArray()?.firstOrNull()
            } catch (exception: Exception) {
               // Timber.tag(TAG).e(exception)
                false
            }
        }

        @JvmStatic
        fun verifyOpenDickyResponse(response: String?): Boolean {
            return try {
                return SUCCESS_OPEN_DICKY_IC.toChar() == response?.toCharArray()?.firstOrNull()
            } catch (exception: Exception) {
               // Timber.tag(TAG).e(exception)
                false
            }
        }
    }
}
