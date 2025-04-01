package com.minamo.mvvm.ext

import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:34
 */


/**
**
* Json str to object
*/
inline fun <reified T> String.fromJson(): T? {
    return kotlin.runCatching {
        val type = object : TypeToken<T>() {}.type
        GsonUtils.fromJson<T>(this, type)
    }.getOrNull()
}

/**
 * Json str to list
 */
inline fun <reified T> String.fromJsonList(): MutableList<T>? {
    val type = GsonUtils.getListType(T::class.java)
    return kotlin.runCatching {
        GsonUtils.fromJson<MutableList<T>>(this, type)
    }.getOrNull()
}

/**
 * Object to json str
 */
inline fun <reified T> T.toJson(includeNulls: Boolean = false): String? {
    return kotlin.runCatching {
        GsonUtils.toJson(Util.getGson(includeNulls), this)
    }.getOrNull()
}

/**
 * MutableList to Json
 */
inline fun <reified T> MutableList<T>.toJson(includeNulls: Boolean = false): String? {
    return kotlin.runCatching {
        GsonUtils.toJson(Util.getGson(includeNulls), this)
    }.getOrNull()
}