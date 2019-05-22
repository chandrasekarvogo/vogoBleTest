package com.vogo.vogobletest.BLE

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.common.io.BaseEncoding
import kotlinx.coroutines.*
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

open class BLEBaseActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val ble: BLE by lazy {
        BLE(mBluetoothAdapter, this)
    }

    private val mBluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var turnOnBluetoothCallback: CompletableDeferred<Boolean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object {
        private val LOGGER = Logger.getLogger(BLEBaseActivity::class.java.name)

        const val REQUEST_OPEN_DICKY = 800
        const val REQUEST_UNLOCK_BIKE = 801
        const val REQUEST_LOCK_BIKE = 802
        const val REQUEST_END_RIDE = 803
        const val REQUEST_CHECK_STATUS = 804
        const val REQUEST_START_RIDE = 805

        const val REQUEST_UNLOCK_IC_BIKE = 900
        const val REQUEST_LOCK_IC_BIKE = 901
        const val REQUEST_END_RIDE_IC = 902
        const val REQUEST_START_RIDE_IC = 903

        const val REQUEST_FUEL_DATA = 1000
        const val REQUEST_BATTERY_LEVEL = 1001
        const val REQUEST_PULSE_COUNTER = 1002
    }

    @IntDef(REQUEST_OPEN_DICKY,
            REQUEST_UNLOCK_BIKE,
            REQUEST_LOCK_BIKE,
            REQUEST_END_RIDE,
            REQUEST_CHECK_STATUS,
            REQUEST_START_RIDE,
            REQUEST_UNLOCK_IC_BIKE,
            REQUEST_LOCK_IC_BIKE,
            REQUEST_END_RIDE_IC,
            REQUEST_START_RIDE_IC)
    annotation class BLERequestType

    enum class BLEType(val code: Int) {
        OPEN_DICKY(REQUEST_OPEN_DICKY),
        UNLOCK_BIKE(REQUEST_UNLOCK_BIKE),
        LOCK_BIKE(REQUEST_LOCK_BIKE),
        END_RIDE(REQUEST_END_RIDE),
        CHECK_STATUS(REQUEST_CHECK_STATUS),
        START_RIDE(REQUEST_START_RIDE),
        UNLOCK_IC_BIKE(REQUEST_UNLOCK_IC_BIKE),
        LOCK_IC_BIKE(REQUEST_LOCK_IC_BIKE),
        END_RIDE_IC(REQUEST_END_RIDE_IC),
        START_RIDE_IC(REQUEST_START_RIDE_IC),
        CHECK_FUEL_IC(REQUEST_FUEL_DATA),
        CHECK_BATTERY_IC(REQUEST_BATTERY_LEVEL),
        CHECK_PULSE_IC(REQUEST_PULSE_COUNTER);

        companion object {
            @JvmStatic
            fun fromCode(value: Int): BLEType? {
                return BLEType.values().firstOrNull {
                    it.code == value
                }
            }
        }
    }

    private suspend fun turnOnBluetoothAndExecute(type: BLEType, block: suspend () -> ByteArray?): ByteArray? {
        when (type) {
            BLEType.OPEN_DICKY,
            BLEType.UNLOCK_BIKE,
            BLEType.LOCK_BIKE,
            BLEType.END_RIDE,
            BLEType.CHECK_STATUS,
            BLEType.START_RIDE,
            BLEType.UNLOCK_IC_BIKE,
            BLEType.LOCK_IC_BIKE,
            BLEType.END_RIDE_IC,
            BLEType.START_RIDE_IC -> {
                return if (mBluetoothAdapter?.isEnabled == false) {
                    turnOnBluetoothCallback = CompletableDeferred()
                    requestEnableBluetooth(type.code)
                    if (turnOnBluetoothCallback!!.await()) {
                        block()
                    } else {
//                        withContext(Dispatchers.Main) {
//                            showToast("Please turn on bluetooth to perform this action")
//                        }
                        null
                    }
                } else {
                    block()
                }
            }
            BLEType.CHECK_FUEL_IC,
            BLEType.CHECK_BATTERY_IC,
            BLEType.CHECK_PULSE_IC -> {
                return if (mBluetoothAdapter?.isEnabled == true) {
                    block()
                } else {
                    LoggerD("Bluetooth turned off")
                    null
                }
            }
        }
    }

    private fun requestEnableBluetooth(requestCode: Int) {
        val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBluetoothIntent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (BLEType.fromCode(requestCode) != null) {
            if (resultCode != Activity.RESULT_OK) {
                showDialog("You need to enable bluetooth to perform this action.",
                        "Enable", Runnable { requestEnableBluetooth(requestCode) }, "Cancel",
                        Runnable { turnOnBluetoothCallback?.complete(false) }
                )
            } else {
                turnOnBluetoothCallback?.complete(true)
            }
        }
    }

    fun showDialog(message: String?, positiveButton: String?, positiveButtonRunnable: Runnable?,
                   negativeButton: String?, negativeButtonRunnable: Runnable?) {
        val endRideRetryDialogBuilder = AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(positiveButton) { dialog, _ ->
                    dialog.dismiss()
                    if (positiveButtonRunnable != null) {
                        runOnUiThread(positiveButtonRunnable)
                    }
                }
                .setCancelable(false)

        if (negativeButton != null) {
            endRideRetryDialogBuilder.setNegativeButton(negativeButton) { dialog, _ ->
                if (negativeButtonRunnable != null) {
                    runOnUiThread(negativeButtonRunnable)
                }
            }
        }
    }

    open fun sendActionCommand(type: BLEType, macAddress: String, block: (type: BLEType, data: ByteArray?) -> Unit) {
        launch(Dispatchers.IO) {
            val byte = sendDataAsync(type, macAddress)
            withContext(Dispatchers.Main) {
                block(type, byte)
            }
            /* sendDataAsync(type, macAddress)?.let {
                 withContext(Dispatchers.Main) {
                     block(type, it)
                 }
             }*/
        }
    }

    private suspend fun sendDataAsync(type: BLEType, macAddress: String): ByteArray? {
        return turnOnBluetoothAndExecute(type) {
            withContext(Dispatchers.Main) {
                showBleLoader(type)
            }

            // Scan for Device with retry
            val device = BLE.withRetry(times = 3) { ble.findDevice(macAddress) }

            val response: ByteArray? = when (device) {
                is BLESuccess -> {
                    LoggerD("Found Successfully")
                    return@turnOnBluetoothAndExecute device.data?.let {
                        LoggerD("Connecting")
                        // Connect with the scanned Device with retry with exponential backoff
                        val pairing = BLE.withRetry(times = 3, initialDelay = 2000) { ble.connectWith(it) }
                        when (pairing) {
                            is BluetoothPairingSuccess -> {
                                LoggerD("Connected Successfully")
                                pairing.data?.let {
                                    val data = getDataFromBLERequestCode(type)
                                    LoggerD("Writing")
                                    // Send data to device
                                    val write = BLE.withRetry(times = 1) { ble.writeData(data, it) }
                                    when (write) {
                                        is WriteSuccess -> {
                                            LoggerD("Written successfully")
//                                            withContext(Dispatchers.Main) {
//                                                hideProgressDialog()
//                                            }
                                            write.data
                                        }
                                        WriteFailure -> {
                                            LoggerD("Writing failure")
//                                            withContext(Dispatchers.Main) {
//                                                hideProgressDialog()
//                                                showToast("Please Turn off ignition. Than try again.")
//                                            }
                                            null
                                        }
                                    }
                                }
                            }
                            BluetoothParingFailure -> {
                                withContext(Dispatchers.Main) {
//                                    hideProgressDialog()
                                    LoggerD("Bluetooth pairing failed")
//                                    showToast("Bluetooth pairing failed")
                                }
                                null
                            }
                        }
                    }
                }
                is BLEFailure -> {
//                    hideProgressDialog()
                    LoggerD("Scanning failed")
                    if (type == BLEType.END_RIDE) {
//                        withContext(Dispatchers.Main) {
//                            showDialog("You are not near the vehicle.", false)
//                        }
                    } else {
//                        withContext(Dispatchers.Main) {
//                            showDialog("You are not near the vehicle.", false)
//                        }
                    }
                    null
                }
            }
            response
        }
    }

    private fun verifyBleDataAndShowMessage(@NonNull byteData: ByteArray, @BLERequestType type: Int) {
        val data = String(byteData, Charsets.UTF_8)
        when (type) {
            REQUEST_LOCK_BIKE -> {
                if (data == BLEConstants.RESPONSE_LOCK_BIKE) {
//                    showToast("Ignition Turned OFF")
                    LoggerD("Lock bike success")
                } else {
                    LoggerD("Lock bike with bluetooth failure")
                    handleConnectionFailed(type)
                }
            }
            REQUEST_UNLOCK_BIKE -> {
                if (data == BLEConstants.RESPONSE_UNLOCK_BIKE) {
                    LoggerD("Ignition Turned ON")
//                    showToast("Bike unlocked")
                } else {
                    LoggerD("Unlock bike with bluetooth failure")
                    handleConnectionFailed(type)
                }
            }
            else -> {
                LoggerD("Invalid request type: $type data: $data")
            }
        }
    }

    // Handle Error cases. (SEND SMS, SHOW ERROR DIALOG or DO NOTHING)
    private fun handleConnectionFailed(@BLERequestType type: Int) {
        when (type) {
            REQUEST_LOCK_BIKE -> {
                lockBike()
            }
            REQUEST_OPEN_DICKY -> {
                // openDicky()
            }
            REQUEST_UNLOCK_BIKE -> {
                unlockBike()
            }
            else -> LoggerD("Invalid BLE Request Type: $type")
        }
    }

    private fun unlockBike() {
        showBleLoader(BLEType.UNLOCK_BIKE)
        AlertDialog.Builder(this)
                .setMessage("Turning the Ignition ON")
                .setPositiveButton("OK") { dialog, _ ->
//                    hideProgressDialog()
                    dialog.dismiss()
                }
                .create()
                .show()
    }

    private fun lockBike() {
        showBleLoader(BLEType.LOCK_BIKE)
        AlertDialog.Builder(this)
                .setMessage("Turning the Ignition OFF")
                .setPositiveButton("OK") { dialog, _ ->
//                    hideProgressDialog()
                    dialog.dismiss()
                }
                .create()
                .show()
    }


    fun showBleLoader(type: BLEType) {
        when (type) {
//            BLEType.END_RIDE_IC, BLEType.END_RIDE -> {
//                showProgressDialog("Checking Ignition Status")
//            }
//            BLEType.LOCK_BIKE, BLEType.LOCK_IC_BIKE -> {
//                showProgressDialog("Turning the Ignition OFF")
//            }
//            BLEType.OPEN_DICKY -> {
//                showProgressDialog("Opening the Seat")
//            }
//            BLEType.UNLOCK_BIKE, BLEType.UNLOCK_IC_BIKE -> {
//                showProgressDialog("Turning the Ignition ON")
//            }
//
//            else -> {
//                showProgressDialog("Please wait..")
//            }
        }
    }

    private fun getDataFromBLERequestCode(code: BLEType): ByteArray {
        return when (code) {
            BLEType.OPEN_DICKY -> {
                BLEConstants.OPEN_DICKY.toByteArray()
            }
            BLEType.UNLOCK_BIKE -> {
                BLEConstants.UNLOCK_BIKE.toByteArray()
            }
            BLEType.LOCK_BIKE -> {
                BLEConstants.LOCK_BIKE.toByteArray()
            }
            BLEType.START_RIDE -> {
                BLEConstants.START_RIDE.toByteArray()
            }
            BLEType.LOCK_IC_BIKE -> {
                BLEConstants.getLockBikeCommand().getByteArray()
            }
            BLEType.UNLOCK_IC_BIKE -> {
                BLEConstants.getUnlockBikeCommand().getByteArray()
            }
            BLEType.START_RIDE_IC -> {
                BLEConstants.getStartRideCommand().getByteArray()
            }
            BLEType.END_RIDE_IC -> {
                BLEConstants.getEndRideCommand().getByteArray()
            }
            BLEType.END_RIDE -> {
                BLEConstants.END_RIDE.toByteArray()
            }
            BLEType.CHECK_STATUS -> {
                BLEConstants.END_RIDE.toByteArray()
            }
            else -> "".getByteArray()
        }
    }

    private fun String.getByteArray(): ByteArray {
        return BaseEncoding.base16().decode(this)
    }

    private fun LoggerD(msg: String) {
        Log.d("BLE - ", msg)
    }
}
