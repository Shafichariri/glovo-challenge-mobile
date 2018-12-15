package com.shafic.challenge.common.base

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.shafic.challenge.navigation.Navigator
import java.lang.ref.WeakReference

abstract class AbstractBaseActivity<B : ViewDataBinding> : AppCompatActivity() {

    //region ABSTRACT PROPERTIES
    abstract val layoutId: Int
    //endregion

    //region ABSTRACT METHODS
    abstract fun onCreated(savedInstanceState: Bundle?)

    abstract fun onCreateViewDataBinding(savedInstanceState: Bundle?): B?
    //endregion

    private var viewDataBinding: B? = null
    private val weakThis: WeakReference<AbstractBaseActivity<B>>? by lazy {
        return@lazy WeakReference<AbstractBaseActivity<B>>(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //This will add instance weak reference to th navigator (following Add calls will replace)
        Navigator.add(weakThis!! as WeakReference<AppCompatActivity>)
        
        viewDataBinding = onCreateViewDataBinding(savedInstanceState)
        onCreated(savedInstanceState)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onStart() {
        Navigator.add(weakThis!! as WeakReference<AppCompatActivity>)
        super.onStart()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onStop() {
        Navigator.remove(weakThis!! as WeakReference<AppCompatActivity>)
        super.onStop()
    }

    protected fun viewBinding(): B? {
        return viewDataBinding
    }
}
