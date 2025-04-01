package com.minamo.mvvm.api.http

import android.util.Log
import com.blankj.utilcode.BuildConfig
import com.blankj.utilcode.util.Utils
import com.minamo.mvvm.api.http.HttpConstant.HTTP_TAG
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:17
 */
object HttpClientHelper {

    fun getOkHttpClient(block: OkHttpClient.Builder.() -> Unit = {}): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        val cacheFile = File(Utils.getApp().cacheDir, "cache")
        val cache = Cache(cacheFile, 1024 * 1024 * 20) //20Mb

        return builder.apply {
            addNetworkInterceptor(httpHeaderInterceptor)
            cache(cache)
            connectTimeout(5, TimeUnit.SECONDS)
            readTimeout(5, TimeUnit.SECONDS)
            writeTimeout(5, TimeUnit.SECONDS)
            connectionSpecs(listOf(
                ConnectionSpec.MODERN_TLS,
                ConnectionSpec.COMPATIBLE_TLS,
                ConnectionSpec.CLEARTEXT)
            )
            retryOnConnectionFailure(false) // 错误重连
            block()
        }.build()
    }

    private fun getHttpLoggingInterceptor(fileClient: Boolean) = HttpLoggingInterceptor(
        object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                if (BuildConfig.DEBUG) {
                    Log.i(HTTP_TAG, message)
                }
            }
        }
    ).apply {
        level = if (fileClient) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.BODY
    }

    /**
     * 防止请求复用导致的：java.io.IOException: unexpected end of stream on okhttp3.Address
     * 第一次请求成功后，客户端复用了原来的连接，但服务器此时已经处在TCP连接中的FIN_WAIT2状态，因此连接不成功
     */
    private object httpHeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val builder = request.newBuilder().addHeader(
                "Connection", "close"
            )
            return chain.proceed(builder.build())
        }
    }
}