package com.minamo.mvvm.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.minamo.mvvm.ext.inflateBindingWithGeneric

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity()  {
    lateinit var binding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBindingWithGeneric(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { parseArguments(it) }
        setupView()
    }

    open fun parseArguments(arguments: Bundle) {}


    open fun setupView() {
        initView()
        initListener()
        initObservable()
    }
    /**
     * view的初始化
     * 例如通过传参进行的界面显示隐藏，recyclerview的初始化。。
     */
    open fun initView() {}

    /**
     * 各类事件回调的监听设置
     */
    open fun initListener() {}

    /**
     * 数据等相关内容的观察监听
     */
    open fun initObservable() {}


    fun <T : ViewModel> getViewModel(modelCass: Class<T>): T {
        val model = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[modelCass]
        return model
    }
}