package com.minamo.mvvm.api.exception

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:15
 * 客户端约定异常
 */
object ClientError {
    /**
     * 未知错误
     */
    const val UNKNOWN = 1000

    /**
     * 解析错误
     */
    const val PARSE_ERROR = 1001

    /**
     * 网络错误
     */
    const val NETWORK_ERROR = 1002

    /**
     * Job 被取消,错误不应该展示给用户
     */
    const val CANCEL_ERROR = 1003
}