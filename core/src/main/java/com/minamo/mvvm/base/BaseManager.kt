package com.minamo.mvvm.base

import androidx.lifecycle.*

/**
 * @author 0xm1nam0
 * @date 2025/4/23 11:46
 */
class BaseManager: LifecycleOwner, ViewModelStoreOwner {

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
//        create()
    }

    open fun create() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    open fun destroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    fun <T : ViewModel> getViewModel(modelCass: Class<T>): T {
        val model = ViewModelProvider(this).get(modelCass)
        return model
    }
}