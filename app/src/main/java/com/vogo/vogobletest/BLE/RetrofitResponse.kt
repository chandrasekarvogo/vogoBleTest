package com.vogo.vogobletest.BLE


sealed class Response<out T>

// T is to avoid type erasures
sealed class RetrofitResponse<out T> : Response<T>()

data class Success<out T> constructor(val data: T?) : RetrofitResponse<T>()

data class Loading constructor(val loading: Boolean = true) : RetrofitResponse<Nothing>()




data class CallError constructor(val throwable: Throwable?) : RetrofitResponse<Nothing>()




sealed class BLEResponse<out T>: Response<T>()

interface BluetoothSuccess

interface BluetoothFailure


sealed class BLEScanningResponse<out T>: BLEResponse<T>()

data class BLESuccess<out T> constructor(val data: T?) : BLEScanningResponse<T>(), BluetoothSuccess

data class BLEFailure constructor(val errorCode: Int? = null): BLEScanningResponse<Nothing>(), BluetoothFailure



sealed class BluetoothParingResponse<out T>: BLEResponse<T>()

data class BluetoothPairingSuccess<out T> constructor(val data: T?): BluetoothParingResponse<T>(), BluetoothSuccess

object BluetoothParingFailure : BluetoothParingResponse<Nothing>(), BluetoothFailure



sealed class WriteResponse<out T>: BLEResponse<T>()

data class WriteSuccess<out T>(val data: T?): WriteResponse<T>(), BluetoothSuccess

object WriteFailure: WriteResponse<Nothing>(), BluetoothFailure


val <T> T.exhaustive: T
    get() = this