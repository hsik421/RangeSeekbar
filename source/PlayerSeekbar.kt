package com.mobile.app.rangeseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min

class RangeSeekbar : FrameLayout {

    lateinit var currentThumb: ImageView

    lateinit var startThumb: ImageView

    lateinit var endThumb: ImageView

    private val rangePaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var rangeColor = 0

    private val trackPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var trackColor = 0

    private var isRange = true

    private var maxProgress = 100
    private var currentProgress = 50f
    private var startProgress = 25f
    private var endProgress = 75f

    private var callback: OnRangeSeekBarListener? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        Log.i("hsik", "init")
        val thumbSize = resources.getDimensionPixelSize(R.dimen.thumb_size)

        currentThumb = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(thumbSize, thumbSize)
            setBackgroundColor(Color.RED)
            setOnTouchListener(thumbCurrentTouch)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_XY
        }

        startThumb = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(thumbSize, thumbSize)
            setBackgroundColor(Color.BLUE)
            setOnTouchListener(thumbStartTouch)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_XY
        }

        endThumb = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(thumbSize, thumbSize)
            setBackgroundColor(Color.GREEN)
            setOnTouchListener(thumbEndTouch)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_XY
        }

        rangeColor = ContextCompat.getColor(context, android.R.color.holo_blue_light)
        rangePaint.apply {
            color = rangeColor
            style = Paint.Style.STROKE
            strokeWidth = context.resources.getDimension(R.dimen.line_height)
        }

        trackColor = ContextCompat.getColor(context, android.R.color.darker_gray)
        trackPaint.apply {
            color = trackColor
            style = Paint.Style.STROKE
            strokeWidth = context.resources.getDimension(R.dimen.line_height)
            alpha = 130
        }



        addView(startThumb)
        addView(endThumb)
        addView(currentThumb)

    }

    fun setRangeMode(isRange: Boolean) {
        this.isRange = isRange
        setThumbVisible()
        invalidate()
    }

    private fun setThumbVisible(){
        startProgress = 0f
        endProgress = maxProgress.toFloat()
        if (isRange) {
            startThumb.visibility = View.VISIBLE
            endThumb.visibility = View.VISIBLE
        } else {
            startThumb.visibility = View.GONE
            endThumb.visibility = View.GONE
        }
    }

    fun getStartProgress(): Int = startProgress.toInt()

    fun getEndProgress(): Int = endProgress.toInt()

    fun getCurrentProgress(): Int = currentProgress.toInt()

    fun setStartProgress(progress: Int) {
        startProgress = progress.toFloat()
        invalidate()
    }

    fun setCurrentProgress(progress: Int) {
        if (isRange && progress > endProgress) {
            callback?.onOverflowValue()
            return
        }
        currentProgress = progress.toFloat()
        invalidate()
    }

    fun setEndProgress(progress: Int) {
        endProgress = progress.toFloat()
        invalidate()
    }

    fun setOnRangeSeekBarListener(listener: OnRangeSeekBarListener) {
        callback = listener
    }

    fun setRangeColor(color: Int) {
        rangeColor = color
        rangePaint.color = color
        invalidate()
    }

    fun setMax(max: Int) {
        maxProgress = max
        invalidate()
    }

    fun drawText(canvas: Canvas?, x: Float, value: Float) {
        canvas?.let {
            if (isRange) {
                val paint = Paint()
                paint.color = Color.parseColor("#3fcfff")
                paint.textSize = 30f
                it.drawText(currentProgress.toLong().durationToText(), x + 5f, 30f, paint)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val dxStart = getDeltaX(startThumb, startProgress)
        val dxCurrent = getDeltaX(currentThumb, currentProgress)
        val dxEnd = getDeltaX(endThumb, endProgress)
        val trackHorizontalPadding = halfWidth(currentThumb).toFloat()
        Log.i("hsik", "dxCurrent = $dxCurrent")
        startThumb.translationX = dxStart
        currentThumb.translationX = dxCurrent
        endThumb.translationX = dxEnd

        if (isRange) {
            canvas?.drawLine(
                trackHorizontalPadding,
                height / 2f,
                trackHorizontalPadding + dxStart,
                height / 2f,
                trackPaint
            )
            canvas?.drawLine(
                trackHorizontalPadding + dxStart,
                height / 2f,
                trackHorizontalPadding + dxEnd,
                height / 2f,
                rangePaint
            )
            canvas?.drawLine(
                trackHorizontalPadding + dxEnd,
                height / 2f,
                width - trackHorizontalPadding,
                height / 2f,
                trackPaint
            )
        } else {
            canvas?.drawLine(
                trackHorizontalPadding,
                height / 2f,
                trackHorizontalPadding + dxCurrent,
                height / 2f,
                rangePaint
            )
            canvas?.drawLine(
                trackHorizontalPadding + dxCurrent,
                height / 2f,
                width - trackHorizontalPadding,
                height / 2f,
                trackPaint
            )
        }

    }

    private fun getDeltaX(thumb: ImageView, progress: Float): Float {
        return (width - thumb.width) * (progress / maxProgress)
    }

    private fun halfWidth(thumb: ImageView): Int = thumb.width / 2

    private val thumbCurrentTouch = object : OnTouchListener {
        var currentX = 0f
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {

            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    currentThumb.isPressed = false
                    v?.performClick()
                    callback?.onStopTrackingTouch()
                }
                MotionEvent.ACTION_DOWN -> {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                    currentX = event.x - event.rawX + currentThumb.translationX
                    currentThumb.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = currentX + event.rawX
                    currentProgress = when {
                        dx < startThumb.x + halfWidth(endThumb) -> {
                            startProgress
                        }
                        dx > endThumb.x + halfWidth(endThumb) -> {
                            endProgress
                        }
                        else -> {
                            (((dx - halfWidth(currentThumb)) / (width - currentThumb.width)) * maxProgress)
                        }
                    }

//                    callback?.onRangeValues(
//                        this@PlayerSeekbar,
//                        progressStart.toInt(),
//                        progressEnd.toInt(),
//                        progressCurrent.toInt()
//                    )
                    Log.i("hsik", "onTouch")
                    invalidate()
                }
                else -> {
                    return false
                }
            }
            return true
        }
    }

    private val thumbStartTouch = object : OnTouchListener {
        var startX = 0f
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    startThumb.isPressed = false
                    v?.performClick()
                }
                MotionEvent.ACTION_DOWN -> {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                    startX = event.x - event.rawX + startThumb.translationX
                    startThumb.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    var dx = startX + event.rawX
                    dx = max(halfWidth(startThumb).toFloat(), dx)
                    startProgress =
                        (((dx - halfWidth(startThumb) ) / (width - startThumb.width)) * maxProgress)
                    if (startProgress >= currentProgress) {
                        startProgress = currentProgress
                    }
                    callback?.onRangeValues(this@RangeSeekbarView, startProgress.toInt(), endProgress.toInt(), currentProgress.toInt())
                    invalidate()
                }
                else -> {
                    return false
                }
            }
            return true
        }
    }

    private val thumbEndTouch = object : OnTouchListener {
        var endX = 0f
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if(!isRange) return false
            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    endThumb.isPressed = false
                    v?.performClick()
                }
                MotionEvent.ACTION_DOWN -> {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                    endX = event.x - event.rawX + endThumb.translationX
                    endThumb.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    var dx = endX + event.rawX
                    dx = min(dx, width.toFloat() - halfWidth(endThumb))
                    endProgress =
                        ((dx - halfWidth(endThumb)) / (width - endThumb.width)) * maxProgress
                    if (endProgress <= currentProgress) {
                        endProgress = currentProgress
                    }
                    callback?.onRangeValues(this@RangeSeekbarView, startProgress.toInt(), endProgress.toInt(), currentProgress.toInt())
                    invalidate()

                }
                else -> {
                    return false
                }
            }
            return true
        }
    }


}

interface OnRangeSeekBarListener {
    fun onRangeValues(rangeSeekBar: RangeSeekbarView, start: Int, end: Int, current: Int)
    fun onStopTrackingTouch()
    fun onOverflowValue()
}

fun Long.durationToText(): String {
    val second = this / 1000 % 60
    val minute = this / 1000 % 3600 / 60
    val hour = this / 1000 / 3600
    return if (hour > 0) {
        String.format("%d:%02d:%02d", hour, minute, second)
    } else {
        String.format("%02d:%02d", minute, second)
    }
}
