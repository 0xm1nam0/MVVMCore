package com.minamo.mvvm.ext

import android.content.res.Resources
import android.graphics.Rect
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:35
 */

fun Float.toPx() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    Resources.getSystem().displayMetrics
)

fun View.runOnUI(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        post {
            block()
        }
    }
}

fun View.visible() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        visibility = View.VISIBLE
    } else {
        post {
            visibility = View.VISIBLE
        }
    }
}

fun View.invisible() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        visibility = View.INVISIBLE
    } else {
        post {
            visibility = View.INVISIBLE
        }
    }
}

fun View.gone() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        visibility = View.GONE
    } else {
        post {
            visibility = View.GONE
        }
    }
}

fun View.setVisible(isVisible: Boolean) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        if (isVisible) {
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    } else {
        post {
            if (isVisible) {
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }
}

fun View.setViewGroupEnabled(enabled: Boolean) {
    runOnUI{
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                val view: View = getChildAt(i)
                view.isEnabled = enabled
                if (view is ViewGroup) {
                    view.setViewGroupEnabled(enabled)
                }
            }
        } else {
            isEnabled = enabled
        }
    }
}

/**
 * 设置防抖动的点击事件
 *
 * @param intervalInMillis 防抖动阈值，默认800ms
 */
fun View.setOnThrottledClickListener(
    intervalInMillis: Long = 800L,
    action: (View) -> Unit
) {
    setOnClickListener(
        object : View.OnClickListener {
            private var lastClickedTimeInMillis: Long = 0

            override fun onClick(v: View) {
                if (abs(System.currentTimeMillis() - lastClickedTimeInMillis) >= intervalInMillis) {
                    lastClickedTimeInMillis = System.currentTimeMillis()
                    action.invoke(v)
                }
            }
        }
    )
}
fun CheckBox.setOnThrottledCheckedListener(
    intervalInMillis: Long = 500L,
    action: (CompoundButton,Boolean) -> Unit
) {
    setOnCheckedChangeListener(
        object : CompoundButton.OnCheckedChangeListener {
            private var lastClickedTimeInMillis: Long = 0

            override fun onCheckedChanged(v: CompoundButton, p1: Boolean) {
                if (abs(System.currentTimeMillis() - lastClickedTimeInMillis) >= intervalInMillis) {
                    lastClickedTimeInMillis = System.currentTimeMillis()
                    action.invoke(v,p1)
                }
            }
        }
    )
}

/**
 * 设置双击事件
 *
 * @param effectiveInMillis 双击的有效时间，默认600ms
 */
fun View.setOnDoubleClickListener(
    effectiveInMillis: Long = 600L,
    onClickAction: (View) -> Unit? = {},
    onDoubleClickAction: (View) -> Unit,
) {
    setOnClickListener(
        object : View.OnClickListener {
            private var lastClickedTimeInMillis: Long = 0
            private var job: Job? = null

            override fun onClick(v: View) {
                if (abs(System.currentTimeMillis() - lastClickedTimeInMillis) < effectiveInMillis) {
                    job?.cancel()
                    lastClickedTimeInMillis = System.currentTimeMillis()
                    onDoubleClickAction.invoke(v)
                } else {
                    lastClickedTimeInMillis = System.currentTimeMillis()
                    job = ThreadScopeExtensions.runOnUI {
                        delay(effectiveInMillis + 20)
                        onClickAction.invoke(v)
                    }
                }
            }
        }
    )
}
/**
 * 判断屏幕上的点是否在View的范围内
 * @receiver View
 * @param pointInScreenX Float
 * @param pointInScreenY Float
 */
fun View.isPointInView(pointInScreenX: Float, pointInScreenY: Float) :Boolean{
    val r = Rect()
    getGlobalVisibleRect(r)
    if (pointInScreenX > r.left && pointInScreenX < r.right && pointInScreenY > r.top && pointInScreenY < r.bottom) {
        return true
    }
    return false
}

/**
 * 设置View的防抖动事件
 * @receiver View
 * @param action Function1<View, Unit>
 */
fun View.setThrottledAction(defaultThrottledTime:Int = 800, action: () -> Unit) {
    val timeKey = "ThrottledAction".hashCode()
    val lastTime = (getTag(timeKey) as Long?) ?: 0
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastTime > defaultThrottledTime) {
        action.invoke()
        setTag(timeKey,currentTime)
    }
}