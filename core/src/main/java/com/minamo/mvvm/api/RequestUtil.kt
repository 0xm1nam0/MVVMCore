package com.minamo.mvvm.api

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.minamo.mvvm.api.exception.ApiException
import com.minamo.mvvm.api.exception.ClientError
import com.minamo.mvvm.api.exception.ExceptionEngine
import com.minamo.mvvm.base.BaseHttpResult
import com.minamo.mvvm.ext.toJson
import kotlinx.coroutines.*

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:24
 */
object RequestUtil {
    private const val TAG = "RequestUtil"

    var requestHandleCallback: RequestHandleCallback?= null

    /**
     * 常规的请求，返回格式同一
     * @param scope CoroutineScope
     * @param block SuspendFunction0<BaseHttpResult<T>?>
     * @param success Function1<T?, Unit>
     * @param error Function1<ApiException, Unit>
     * @return Job
     */
    internal fun <T> request(
        scope: CoroutineScope,
        block: suspend () -> BaseHttpResult<T>?,
        success: (T?) -> Unit,
        error: (ApiException) -> Unit = {}
    ): Job {
        return scope.launch(Dispatchers.Main) {
            runCatching {
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                //网络请求成功
                try {
                    //因为要判断请求的数据结果是否成功，失败会抛出自定义异常，所以在这里try一下
                    executeResponse(scope, it!!,error, { tIt -> success(tIt) })
                } catch (e: Exception) {
                    //失败回调
                    handleException(e, error)
                }
            }.onFailure { e ->
                //失败回调
                handleException(e, error)
            }
        }
    }
    internal fun <T> request(
        scope: CoroutineScope,
        blocks: List<suspend () -> BaseHttpResult<T>?>,  // 支持多个请求
        success: (List<T?>) -> Unit,  // 成功回调接收多个结果
        error: (ApiException) -> Unit = {}
    ): Job {
        return scope.launch(Dispatchers.Main) {
            val results = mutableListOf<T?>()
            var hasFailed = false  // 标记是否发生过失败
            // 串行执行每个请求
            for (block in blocks) {
                try {
                    // 1. 执行请求
                    val result = withContext(Dispatchers.IO) { block() }

                    // 2. 如果请求返回的结果为空，加入 null
                    result?.let {
                        try {
                            // 3. 处理请求的结果
                            executeResponse(scope, it, error) { tIt ->
                                // 保存处理后的结果
                                results.add(tIt)
                            }
                        } catch (e: Exception) {
                            // 4. 如果处理结果时出错，记录错误并标记跳过当前请求
                            handleException(e, error)
                            hasFailed = true  // 设置标志跳过当前请求
                        }
                    } ?: results.add(null)  // 如果结果为空，则加入 null

                } catch (e: Exception) {
                    // 5. 请求执行时出错，捕获异常并标记跳过当前请求
                    handleException(e, error)
                    hasFailed = true  // 设置标志跳过当前请求
                }

                // 6. 如果当前请求出错，跳过后续的操作，继续下一个请求
                if (hasFailed) {
                    break  // 跳过当前请求，继续执行下一个请求
                }
            }

            if (!hasFailed) {
                // 7. 所有请求完成后，执行 success 回调
                success(results)
            }
        }
    }



    private fun handleException(e: Throwable, error: (ApiException) -> Unit = {}) {
        val apiException = ExceptionEngine.handleException(e)
        if (apiException.code != ClientError.CANCEL_ERROR) {
            error(apiException)
        }
        if (apiException.code == ExceptionEngine.UNAUTHORIZED) {
            //UNAUTHORIZED
            LogUtils.d("handleException UNAUTHORIZED")
            requestHandleCallback?.logout()
        }
    }

    /**
     * 非常规请求，返回格式没有统一
     * @param scope CoroutineScope
     * @param block SuspendFunction0<T?>
     * @param success Function1<T?, Unit>
     * @param error Function1<ApiException, Unit>
     * @return Job
     */
    internal fun <T> requestWithoutExecute(
        scope: CoroutineScope,
        block: suspend () -> T?,
        success: (T?) -> Unit,
        error: (ApiException) -> Unit = {}
    ): Job {
        return scope.launch(Dispatchers.Main) {
            runCatching {
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                //网络请求成功
                try {
                    success(it)
                } catch (e: Exception) {
                    //失败回调
                    val apiException = ExceptionEngine.handleException(e)
                    if (apiException.code != ClientError.CANCEL_ERROR) {
                        error(apiException)
                    }
                }
            }.onFailure { e ->
                //失败回调
                val apiException = ExceptionEngine.handleException(e)
                if (apiException.code != ClientError.CANCEL_ERROR) {
                    error(apiException)
                }
            }
        }
    }

    /**
     * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则抛出异常
     */
    private fun <T> executeResponse(
        scope: CoroutineScope,
        response: BaseHttpResult<T>,
        error: (ApiException) -> Unit = {},
        success: (T?) -> Unit,
    ) {
        if (scope is LifecycleCoroutineScope) {
            scope.launchWhenCreated {
                kotlin.runCatching {
                    executeResponse(response, success)
                }.onFailure {
                    handleException(it,error)
                }
            }
        } else {
            executeResponse(response, success)
        }
    }

    /**
     * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则抛出异常
     */
    private fun <T> executeResponse(
        response: BaseHttpResult<T>,
        success: (T?) -> Unit,
    ) {
        when {
            response.isSuccess() -> {
                success(response.getData())
            }
            response.isTokenInvalid() -> {
                //token失效
                requestHandleCallback?.logout()
            }
            requestHandleCallback?.handleResponse(response)!! -> {
            }
            else -> {
                LogUtils.eTag(TAG, response.toJson())
                throw ApiException(code = response.getCode()!!.toInt(), message = response.getMsg())
            }
        }
    }
}

interface RequestHandleCallback {
    fun logout()

    fun <T> handleResponse(response: BaseHttpResult<T>): Boolean
}

//    ------------------ Global ----------------
fun <T> globalRequest(
    block: suspend () -> BaseHttpResult<T>?,
    success: (T?) -> Unit,
    error: (ApiException) -> Unit = {},
): Job {
    return RequestUtil.request(GlobalScope, block, success, error)
}

fun <T> globalRequestWithoutExecute(
    block: suspend () -> T?,
    success: (T?) -> Unit,
    error: (ApiException) -> Unit = {}
): Job {
    return RequestUtil.requestWithoutExecute(GlobalScope, block, success, error)
}

//    ------------------ lifecycleScope ----------------
fun <T> LifecycleOwner.request(
    block: suspend () -> BaseHttpResult<T>?,
    success: (T?) -> Unit,
    error: (ApiException) -> Unit = {},
): Job {
    return RequestUtil.request(lifecycleScope, block, success, error)
}

fun <T> LifecycleOwner.requestWithoutExecute(
    block: suspend () -> T?,
    success: (T?) -> Unit,
    error: (ApiException) -> Unit = {}
): Job {
    return RequestUtil.requestWithoutExecute(lifecycleScope, block, success, error)
}

//    ------------------ CoroutineScope ----------------
fun <T> CoroutineScope.request(
    block: suspend () -> BaseHttpResult<T>?,
    success: (T?) -> Unit,
    error: (ApiException) -> Unit = {},
): Job {
    return RequestUtil.request(this, block, success, error)
}