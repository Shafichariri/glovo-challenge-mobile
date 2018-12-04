package com.shafic.challenge.common.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment

class AdvancedGoogleMapFragment : SupportMapFragment() {
    private var mMapScrollListener: MapScrollListener? = null
    var isTouchAllowed = true
        private set  // consider moving to inner frame

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context
        if (context != null) {
            val sensitiveFrame = ScrollTracker(context)
            val viewGroup = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup?

            sensitiveFrame.addView(viewGroup)
            return sensitiveFrame
        }
        return null
    }

    private fun onMapClicked() {
        if (mMapScrollListener != null) {
            mMapScrollListener!!.onMapClicked()
        }
    }

    private fun onScrollStarted() {
        if (mMapScrollListener != null) {
            mMapScrollListener!!.onMapScrollStarted()
        }
    }

    private fun onScrollEnded() {
        if (mMapScrollListener != null) {
            mMapScrollListener!!.onMapScrollEnded()
        }
    }

    fun setMapScrollListener(listener: MapScrollListener) {
        this.mMapScrollListener = listener
    }

    fun allowScroll(allow: Boolean) {
        isTouchAllowed = allow
    }

    interface MapScrollListener {
        fun onMapScrollStarted()
        fun onMapClicked()
        fun onMapScrollEnded()
    }

    private inner class ScrollTracker(context: Context) : FrameLayout(context) {
        private val SCROLL_THRESHOLD = 10f
        private val EVENT_IDLE = 0
        private val EVENT_CLICK = 1
        private val EVENT_MOVE = 2

        private var mDownX: Float = 0.toFloat()
        private var mDownY: Float = 0.toFloat()

        private var mEventMode: Int = 0
        private var mPostponeScrollEnd: Boolean = false

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDownX = event.x
                    mDownY = event.y
                    mEventMode = EVENT_CLICK
                }

                MotionEvent.ACTION_MOVE -> if (mEventMode == EVENT_CLICK && (Math.abs(mDownX - event.x) > SCROLL_THRESHOLD || Math.abs(
                        mDownY - event.y
                    ) > SCROLL_THRESHOLD)
                ) {
                    mEventMode = EVENT_MOVE
                    onScrollStarted()
                }

                MotionEvent.ACTION_UP -> if (mEventMode == EVENT_MOVE) {
                    mEventMode = EVENT_IDLE

                    if (mPostponeScrollEnd) {
                        mPostponeScrollEnd = false
                        onScrollEnded()
                    }
                } else if (mEventMode == EVENT_CLICK) {
                    mEventMode = EVENT_IDLE
                    onMapClicked()
                }
            }

            // return true to block motion
            return if (isTouchAllowed) super.dispatchTouchEvent(event) else true
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            getMapAsync { googleMap ->
                //TODO: Replace deprecated with setOnCameraMoveListener and others
                googleMap.setOnCameraChangeListener {
                    if (mEventMode == EVENT_IDLE) {
                        mPostponeScrollEnd = false
                        onScrollEnded()
                    } else {
                        mPostponeScrollEnd = true
                    }
                }
            }
        }
    }

    companion object {

        fun newInstance(): AdvancedGoogleMapFragment {
            return AdvancedGoogleMapFragment()
        }

        fun newInstance(options: GoogleMapOptions): AdvancedGoogleMapFragment {
            val var1 = AdvancedGoogleMapFragment()
            val var2 = Bundle()
            var2.putParcelable("MapOptions", options)
            var1.arguments = var2
            return var1
        }
    }
}
