package com.shafic.challenge.common.base

import android.support.v7.util.DiffUtil

abstract class AbstractDiffUtilsCallback<T>(private val oldList: List<T>, private val newList: List<T>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }
}
