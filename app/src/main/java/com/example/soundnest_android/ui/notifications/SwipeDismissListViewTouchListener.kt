package com.example.soundnest_android.ui.notifications

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ListView
import android.widget.AdapterView
import android.widget.AbsListView
import android.widget.Toast

@SuppressLint("ClickableViewAccessibility")
class SwipeDismissListViewTouchListener(
    listView: ListView,
    dismissCallbacks: DismissCallbacks
) : View.OnTouchListener {

    private val mListView: ListView = listView
    private val mCallbacks: DismissCallbacks = dismissCallbacks
    private val mGestureDetector: GestureDetector

    interface DismissCallbacks {
        fun canDismiss(position: Int): Boolean
        fun onDismiss(listView: ListView?, position: Int)
    }

    init {
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val position = mListView.pointToPosition(e1!!.x.toInt(), e1.y.toInt())
                if (position != AdapterView.INVALID_POSITION && mCallbacks.canDismiss(position)) {
                    mCallbacks.onDismiss(mListView, position)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }

        mGestureDetector = GestureDetector(listView.context, gestureListener)
        listView.setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }
}
