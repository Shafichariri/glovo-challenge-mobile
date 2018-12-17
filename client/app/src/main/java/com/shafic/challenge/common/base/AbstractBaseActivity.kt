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
        updateNavigatorState(Action.add)

        viewDataBinding = onCreateViewDataBinding(savedInstanceState)
        onCreated(savedInstanceState)
    }

    override fun onStart() {
        updateNavigatorState(Action.add)
        super.onStart()
    }

    override fun onStop() {
        updateNavigatorState(Action.remove)
        super.onStop()
    }

    override fun onResume() {
        updateNavigatorState(Action.add)
        super.onResume()
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateNavigatorState(action: Action) {
        when (action) {
            Action.add -> Navigator.add(weakThis!! as WeakReference<AppCompatActivity>)
            Action.remove -> Navigator.remove(weakThis!! as WeakReference<AppCompatActivity>)
        }
    }

    protected fun viewBinding(): B? {
        return viewDataBinding
    }


    private enum class Action {
        add,
        remove
    }
}
