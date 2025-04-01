package com.minamo.mvvm.ext

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:36
 */
object Util {
    /**
     * 保留空字段
     */
    private val gsonNoNull by lazy { GsonBuilder().create() }

    /**
     * 自动过滤空字段
     */
    private val gson by lazy { GsonBuilder().serializeNulls().create() }

    fun getGson(includeNulls: Boolean = false): Gson {
        return if (includeNulls) gsonNoNull else gson
    }
}