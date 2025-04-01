package com.minamo.mvvm.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.minamo.mvvm.ext.inflateBindingWithGeneric

/**
 * @author 0xm1nam0
 * @date 2025/4/1 21:12
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    val TAG = this.javaClass.simpleName
    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = inflateBindingWithGeneric(inflater, container, false)
        arguments?.let { parseArguments(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObservable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    open fun parseArguments(arguments: Bundle) {}

    /**
     * view的初始化
     * 例如通过传参进行的界面显示隐藏，recyclerview的初始化。。
     */
     fun initView() {}

    /**
     * 各类事件回调的监听设置
     */
     fun initListener() {}

    /**
     * 数据等相关内容的观察监听
     */
    fun initObservable() {}

    fun <T : BaseViewModel> getViewModel(modelCass: Class<T>): T {
        val model = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[modelCass]
        return model
    }

    fun <T : BaseViewModel> getParentViewModel(modelCass: Class<T>): T {
        val model = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[modelCass]
        return model
    }
}