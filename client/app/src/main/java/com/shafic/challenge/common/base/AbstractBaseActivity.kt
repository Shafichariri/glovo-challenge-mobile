package com.shafic.challenge.common.base

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class AbstractBaseActivity<B : ViewDataBinding> : AppCompatActivity() {

    //region ABSTRACT PROPERTIES
    abstract val layoutId: Int
    //endregion
    
    //region ABSTRACT METHODS
    abstract fun onCreated(savedInstanceState: Bundle?)

    abstract fun onCreateViewDataBinding(savedInstanceState: Bundle?): B?
    //endregion

    private var viewDataBinding: B? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = onCreateViewDataBinding(savedInstanceState)
        onCreated(savedInstanceState)
    }

    protected fun viewBinding(): B? {
        return viewDataBinding
    }
}
