package com.minamo.mvvm.api.exception

import android.net.ParseException
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.google.gson.JsonParseException
import com.minamo.mvvm.R
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:15
 */
object ExceptionEngine {
    private val TAG = this.javaClass.simpleName
    //对应HTTP的状态码
    const val UNAUTHORIZED = 401
    private const val FORBIDDEN = 403
    private const val NOT_FOUND = 404
    private const val REQUEST_TIMEOUT = 408
    private const val INTERNAL_SERVER_ERROR = 500
    private const val BAD_GATEWAY = 502
    private const val SERVICE_UNAVAILABLE = 503
    private const val GATEWAY_TIMEOUT = 504

    fun handleException(e: Throwable): ApiException {
        LogUtils.eTag(TAG, "handleException message",e)
        return when (e) {
            is HttpException -> ApiException(e, code = e.code()).apply {
                message = when (code) {
                    UNAUTHORIZED, FORBIDDEN, NOT_FOUND, REQUEST_TIMEOUT, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE -> "网络错误"
                    else -> Utils.getApp().getString(R.string.libutil_network_error_request_failed)
                }
            }
            is JSONException,
            is ParseException,
            is JsonParseException -> ApiException(e, code = ClientError.PARSE_ERROR).apply { message = Utils.getApp().getString(R.string.libutil_json_parse_failed) }
            is UnknownHostException -> ApiException(e, code = ClientError.NETWORK_ERROR).apply { message = Utils.getApp().getString(R.string.libutil_network_error_network_host_unknown) }
            is SocketTimeoutException -> ApiException(e, code = ClientError.NETWORK_ERROR).apply { message = Utils.getApp().getString(R.string.libutil_network_error_timeout) }
            is ConnectException -> ApiException(e, code = ClientError.NETWORK_ERROR).apply { message = Utils.getApp().getString(R.string.libutil_network_connect_error) }
            is ApiException -> e
            is CancellationException -> ApiException(e, code = ClientError.CANCEL_ERROR).apply { message = Utils.getApp().getString(R.string.libutil_network_error_unknown_error) }
            else -> ApiException(e, code = ClientError.UNKNOWN).apply { message = e.message }
        }
    }
}