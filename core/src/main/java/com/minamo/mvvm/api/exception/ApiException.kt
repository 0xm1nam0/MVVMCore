package com.minamo.mvvm.api.exception

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:15
 */
class ApiException(
    throwable: Throwable? = null,
    override var message: String? = null,
    val code: Int = 0
) : Exception(message, throwable)