package com.minamo.mvvm.base

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:11
 */
abstract class BaseHttpResult<T> {
    abstract fun isSuccess(): Boolean
    abstract fun isTokenInvalid(): Boolean
    //abstract fun isShowToast(): Boolean

    //String 兼容int和String类型
    abstract fun getCode(): String?
    abstract fun getMsg(): String?
    abstract fun getData(): T?
}