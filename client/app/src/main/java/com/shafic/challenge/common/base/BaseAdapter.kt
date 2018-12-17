package com.shafic.challenge.common.base

import android.content.Context
import android.databinding.ViewDataBinding
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import java.util.*

abstract class DataBoundViewHolder<BINDING : ViewDataBinding>(internal val viewBinding: BINDING) :
    BaseViewHolder(viewBinding.root)

abstract class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

open class AdapterItem(val id: String, val layoutId: Int)

abstract class BaseAdapter<VH : BaseViewHolder, ITEM : AdapterItem>(
    val context: Context, var data: MutableList<ITEM> = ArrayList()
) : RecyclerView.Adapter<VH>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var onItemClickListener: OnItemClickListener<ITEM>? = null

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener { onItemClickListener!!.onItemClick(position, data[position]) }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].layoutId
    }

    open fun update(newItems: List<ITEM>) {
        val diffResult = DiffUtil.calculateDiff(DefaultDiffUtilsCallback(data, newItems))
        diffResult.dispatchUpdatesTo(this)

        data.clear()
        data.addAll(newItems)
    }

    interface OnItemClickListener<ITEM> {
        fun onItemClick(position: Int, item: ITEM)
    }
}

