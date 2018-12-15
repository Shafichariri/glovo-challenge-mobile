package com.shafic.challenge.common.base

open class DefaultDiffUtilsCallback<T : AdapterItem>(private val oldList: List<T>, private val newList: List<T>) :
    AbstractDiffUtilsCallback<T>(oldList, newList) {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Data.getId() is String
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
