package com.minamo.mvvm.utils

/**
 * @author  Minamo
 * @e-mail  kleinminamo@gmail.com
 * @time    2021/12/17
 * @des     GenericCallbacks
 */
class GenericCallbacks {

    interface CallbackWithTwoParam<X, Y> {
        fun onSuccess(var1: X, var2: Y)
        fun onFailure(e: Exception?)
    }

    interface CallbackWith<T> {
        fun onSuccess(var1: T)
        fun onFailure(e: Exception?)
    }

    interface Callback<T : Exception?> {
        fun onResult(e: T)
    }
}