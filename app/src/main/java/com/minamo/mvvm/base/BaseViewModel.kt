package com.minamo.mvvm.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minamo.mvvm.api.RequestUtil
import com.minamo.mvvm.api.exception.ApiException

open class BaseViewModel : ViewModel() {
    val TAG = this.javaClass.simpleName

    fun <T> request(
        block: suspend () -> BaseHttpResult<T>?,
        success: (T?) -> Unit,
        error: (ApiException) -> Unit = {}
    ) {
        RequestUtil.request(viewModelScope,block,success, error)
    }

    fun <T> request(
        blocks: List<suspend () -> BaseHttpResult<T>?>,
        success: (List<T?>) -> Unit,
        error: (ApiException) -> Unit = {}
    ) {
        RequestUtil.request(viewModelScope,blocks,success, error)
    }
    @Deprecated("所有接口请求已经有统一的结果返回，该api应该是不需要了！！")
    fun <T> requestWithoutExecute(
        block: suspend () -> T?,
        success: (T?) -> Unit,
        error: (ApiException) -> Unit = {}
    ) {
        RequestUtil.requestWithoutExecute(viewModelScope,block,success, error)
    }
}