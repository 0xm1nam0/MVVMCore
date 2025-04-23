package com.minamo.mvvm.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.*

/**
 * @author  Minamo
 * @e-mail  kleinminamo@gmail.com
 * @time    2021/8/4
 * @des     BaseService
 */
open abstract class BaseService : Service(), LifecycleOwner, ViewModelStoreOwner {

    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val viewModelStore = ViewModelStore()
    override val lifecycle = lifecycleRegistry


    init {
        lifecycle.addObserver(LifecycleEventObserver { source, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // And clear the ViewModelStore
                viewModelStore.clear()
            }
        })
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    fun <T : BaseViewModel> getViewModel(modelCass: Class<T>): T {
        val model = ViewModelProvider(this).get(modelCass)
        return model
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}