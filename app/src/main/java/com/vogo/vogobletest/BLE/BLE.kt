package com.vogo.vogobletest.BLE

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log

import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.resume


class BLE(private val mBluetoothAdapter: BluetoothAdapter?,
          private val context: Context) {
    companion object {
        private val TAG = BLE::class.java.simpleName
        private const val SCAN_PERIOD = 7000L
        private const val CONNECT_TIMEOUT = 7000L
        private const val WRITE_TIMEOUT = 10000L

        @JvmStatic
        suspend fun <U, T : BLEResponse<U>> withRetry(
                times: Int = Int.MAX_VALUE,
                initialDelay: Long = 1000, // 1 second
                maxDelay: Long = 5000,    // 1 second
                factor: Double = 2.0,
                block: suspend () -> T): T {
            var currentDelay = initialDelay
            repeat(times - 1) {
                val response = block()
                if (response is BluetoothSuccess) {
                    return response
                }
                //  Timber.tag(TAG).d("Failed.. Trying again.")
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
            return block() // last attempt
        }
    }

    private var scanning: Boolean = false

    private var mScanCallback: ScanCallback? = null
    private lateinit var settings: ScanSettings

    private var leScanCallback: BluetoothAdapter.LeScanCallback? = null

    private var bleConnectionState: BluetoothState? = BluetoothState.DISCONNECTED

    private lateinit var macToFilter: String

    private var mGatt: BluetoothGatt? = null

    var searchBLEListener: CompletableDeferred<BLEScanningResponse<BluetoothDevice>>? = null

    var connectBLEListener: CompletableDeferred<BluetoothParingResponse<BluetoothGattCharacteristic>>? = null

    var writeAcknowledgement: CompletableDeferred<WriteResponse<ByteArray>>? = null

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            characteristic?.value?.let {
                writeAcknowledgement?.complete(WriteSuccess(it))
            } ?: writeAcknowledgement?.complete(WriteFailure)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
                        ?.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))?.let {
                            enableNotification(it)
                            connectBLEListener?.complete(BluetoothPairingSuccess(it))
                        } ?: connectBLEListener?.complete(BluetoothParingFailure)
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_DISCONNECTED -> {
                    clear()
                    bleConnectionState = BluetoothState.DISCONNECTED
                }
                BluetoothGatt.STATE_CONNECTING -> bleConnectionState = BluetoothState.CONNECTING
                BluetoothGatt.STATE_CONNECTED -> {
                    gatt?.discoverServices()
                    bleConnectionState = BluetoothState.CONNECTED
                }
                BluetoothGatt.STATE_DISCONNECTING -> {
                    bleConnectionState = BluetoothState.DISCONNECTING
                }
            }
        }
    }


    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
            mScanCallback = object : ScanCallback() {
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    searchBLEListener?.complete(BLEFailure(errorCode))
                }

                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    result?.device?.let {
                        if (it.address.equals(macToFilter, true)) {
                            searchBLEListener?.complete(BLESuccess(it))
                        }
                    }
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    val result = results?.find {
                        it.device.address.equals(macToFilter, true)
                    }
                    if (result == null) {
                        searchBLEListener?.complete(BLEFailure())
                    }
                }
            }
        } else {
            leScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
                device?.let {
                    searchBLEListener?.complete(BLESuccess(it))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun enableNotification(characteristic: BluetoothGattCharacteristic) {
        mGatt?.let { gatt ->
            characteristic.descriptors.forEach {
                it?.let { descriptor ->
                    descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
//                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }

            gatt.setCharacteristicNotification(characteristic, true)
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    suspend fun writeData(data: ByteArray, characteristic: BluetoothGattCharacteristic) =
            suspendCancellableCoroutine<WriteResponse<ByteArray>> { continuation ->
                mGatt?.let {
                    runBlocking {
                        // Delay for better success rate
                        delay(500)
                        writeAcknowledgement = CompletableDeferred()
                        characteristic.value = data
                        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        it.writeCharacteristic(characteristic)
                        val response = withTimeoutOrNull(WRITE_TIMEOUT) {
                            writeAcknowledgement?.await()
                        }
                        if (response != null) {
                            continuation.resume(response)
                        } else {
                            continuation.resume(WriteFailure)
                        }
                        clear()
                    }
                }
            }

    suspend fun connectWith(device: BluetoothDevice) =
            suspendCancellableCoroutine<BluetoothParingResponse<BluetoothGattCharacteristic>> { continuation ->
                runBlocking {
                    connectBLEListener = CompletableDeferred()
                    mGatt = device.connectGatt(context, false, gattCallback)
                    val callback: BluetoothParingResponse<BluetoothGattCharacteristic>? =
                            withTimeoutOrNull(CONNECT_TIMEOUT) {
                                connectBLEListener?.await()
                            }
                    when (callback) {
                        is BluetoothPairingSuccess -> {
                            continuation.resume(callback)
                        }
                        else -> {
                            continuation.resume(BluetoothParingFailure)
                            clear()
                        }
                    }
                }
            }

    suspend fun findDevice(macAddress: String) =
            suspendCancellableCoroutine<BLEScanningResponse<BluetoothDevice>> { continuation ->
                if (!scanning) {
                    scanning = true
                    this.macToFilter = macAddress
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothAdapter?.bluetoothLeScanner?.startScan(mutableListOf(), settings, mScanCallback)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        mBluetoothAdapter?.startLeScan(leScanCallback)
                    } else {
                        continuation.resume(BLEFailure())
                        return@suspendCancellableCoroutine
                    }
                    searchBLEListener = CompletableDeferred()
                    var response: BLEScanningResponse<BluetoothDevice>? = null
                    runBlocking {
                        response = withTimeoutOrNull(SCAN_PERIOD) {
                            searchBLEListener?.await()
                        }
                    }
                    if (response != null) {
                        stopScan()
                        continuation.resume(response!!)
                    } else {
                        clear()
                        continuation.resume(BLEFailure())
                    }
                } else {
                    Log.w(TAG, "Already scanning for BLE Devices")
                    continuation.resume(BLEFailure())
                }
            }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun findDevices(scanCallback: ScanCallback, scanTimeout: Long) =
            suspendCancellableCoroutine<Boolean> { continuation ->
                if (!scanning) {
                    scanning = true
                    mBluetoothAdapter?.bluetoothLeScanner?.startScan(mutableListOf(), settings, scanCallback)
                    searchBLEListener = CompletableDeferred()
                    runBlocking {
                        delay(scanTimeout)
                        stopScan()
                        continuation.resume(true)
                    }
                } else {
                    Log.w(TAG, "Already scanning for BLE Devices")
                    continuation.resume(false)
                }
            }

    suspend fun findDevices(scanCallback: BluetoothAdapter.LeScanCallback, scanTimeout: Long) =
            suspendCancellableCoroutine<Boolean> { continuation ->
                if (!scanning) {
                    scanning = true
                    mBluetoothAdapter?.startLeScan(scanCallback)
                    runBlocking {
                        delay(scanTimeout)
                        stopScan()
                        continuation.resume(true)
                    }
                } else {
                    Log.w(TAG, "Already scanning for BLE Devices")
                    continuation.resume(false)
                }
            }



    private fun stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter?.bluetoothLeScanner?.stopScan(mScanCallback)
            mBluetoothAdapter?.bluetoothLeScanner?.flushPendingScanResults(mScanCallback)
        } else {
            mBluetoothAdapter?.stopLeScan(leScanCallback)
        }
        scanning = false
    }

    fun clear() {
        stopScan()
        mGatt?.apply {
            disconnect()
            close()
        }
    }
}

enum class BluetoothState(val value: Int) {
    DISCONNECTED(0),
    CONNECTING(1),
    CONNECTED(3),
    DISCONNECTING(3)
}


