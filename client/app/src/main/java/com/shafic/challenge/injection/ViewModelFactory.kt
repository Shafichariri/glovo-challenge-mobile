package com.shafic.challenge.injection

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.shafic.challenge.ui.map.MainActivityViewModel

class ViewModelFactory : ViewModelProvider.Factory {
    //We add to the construct any property needed for the VM initialization [If Any, ex: Context for Getting DB Instance]
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
