package com.mobile.app.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.hackers.app.appplayer.R
import kotlin.math.max
import kotlin.math.min


class PlayerSeekbar : FrameLayout {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private lateinit var thumbStart: Thumb
    private var progressStart = 0f

    private lateinit var thumbCurrent: Thumb
    private var progressCurrent = 0f

    private lateinit var thumbEnd: Thumb
    private var progressEnd = 50f

    private lateinit var container: FrameLayout

    private var minDifference = 20

    private val rangePaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var rangeColor = 0

    private val trackPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var trackColor = 0

    private var callback: OnRangeSeekBarListener? = null

    private lateinit var containerLayoutParams: LayoutParams

    private var isRange = false

    private var maxProgress = 100

    private fun init() {
        setWillNotDraw(false)

        val containerHeight = resources.getDimensionPixelSize(R.dimen.container_height)
        val containerMargin = resources.getDimensionPixelSize(R.dimen.container_margin)

        container = FrameLayout(context)
        containerLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, containerHeight).apply {
            gravity = Gravity.CENTER
            leftMargin = containerMargin
            rightMargin = containerMargin
        }
        addView(container, containerLayoutParams)



        thumbStart = Thumb(context).apply {
            val thumbStartLayoutParams = LayoutParams(getThumbWidth(), getThumbWidth())
            thumbStartLayoutParams.gravity = Gravity.CENTER_VERTICAL
            container.addView(this, thumbStartLayoutParams)
            setOnTouchListener(thumbStartTouch)
        }

        thumbEnd = Thumb(context).apply {
            val thumbEndLayoutParams = LayoutParams(getThumbWidth(), getThumbWidth())
            thumbEndLayoutParams.gravity = Gravity.CENTER_VERTICAL
            container.addView(this, thumbEndLayoutParams)
            setOnTouchListener(thumbEndTouch)
        }

        thumbCurrent = Thumb(context).apply {
            val thumbCurrentLayoutParams = LayoutParams(getThumbWidth(), getThumbWidth())
            setColor(Color.RED)
            thumbCurrentLayoutParams.gravity = Gravity.CENTER_VERTICAL
            container.addView(this, thumbCurrentLayoutParams)
            setOnTouchListener(thumbCurrentTouch)
        }

        rangeColor = ContextCompat.getColor(context, Thumb.getStyledValueFor(context, R.attr.colorControlActivated))
        rangePaint.apply {
            color = rangeColor
            style = Paint.Style.STROKE
            strokeWidth = context.resources.getDimension(R.dimen.line)
        }

        trackColor = ContextCompat.getColor(context, android.R.color.darker_gray)
        trackPaint.apply {
            color = trackColor
            style = Paint.Style.STROKE
            strokeWidth = context.resources.getDimension(R.dimen.line)
            alpha = 130
        }

        thumbStart.setDisableCircleColor(trackColor)
        thumbEnd.setDisableCircleColor(trackColor)
        thumbCurrent.setDisableCircleColor(trackColor)

        setThumbVisible()
    }

    fun setRangeColor(color: Int) {
        rangeColor = color
        rangePaint.color = color
        thumbStart.setColor(color)
        thumbEnd.setColor(color)
        invalidate()
    }

    fun setTrackColor(color: Int) {
        trackColor = color
        trackPaint.color = color
        thumbStart.setDisableCircleColor(color)
        thumbEnd.setDisableCircleColor(color)
        invalidate()
    }

    fun setMax(max: Int) {
        maxProgress = max
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val dxStart = getDeltaX(thumbStart, progressStart)
        thumbStart.translationX = dxStart

        val dxCurrent = getDeltaX(thumbCurrent, progressCurrent)
        thumbCurrent.translationX = dxCurrent

        val dxEnd = getDeltaX(thumbEnd, progressEnd)
        thumbEnd.translationX = dxEnd

        if (dxStart > rangePaint.strokeWidth * 3) {
            canvas?.drawLine(
                thumbStart.getHalfThumbWidth() + containerLayoutParams.leftMargin.toFloat(),
                height / 2f,
                dxStart + thumbStart.getHalfThumbWidth() + containerLayoutParams.leftMargin - (rangePaint.strokeWidth * 3),
                height / 2f,
                trackPaint
            )
        }
        canvas?.drawLine(
            dxStart + thumbStart.getHalfThumbWidth() + containerLayoutParams.leftMargin + (rangePaint.strokeWidth * 3),
            height / 2f,
            dxEnd + thumbEnd.getHalfThumbWidth() + containerLayoutParams.rightMargin - (rangePaint.strokeWidth * 3),
            height / 2f,
            trackPaint
        )
        if (container.width - containerLayoutParams.leftMargin - containerLayoutParams.rightMargin > dxEnd + thumbEnd.getHalfThumbWidth() + containerLayoutParams.rightMargin + (rangePaint.strokeWidth * 3)) {
            canvas?.drawLine(
                dxEnd + thumbEnd.getHalfThumbWidth() + containerLayoutParams.rightMargin + (rangePaint.strokeWidth * 3),
                height / 2f,
                container.width.toFloat() - containerLayoutParams.leftMargin - containerLayoutParams.rightMargin,
                height / 2f,
                trackPaint
            )
        }
        if(isRange){
            canvas?.drawLine(
                dxStart + thumbStart.getHalfThumbWidth() + containerLayoutParams.leftMargin,
                height / 2f,
                dxEnd + thumbEnd.getHalfThumbWidth() + containerLayoutParams.rightMargin,
                height / 2f,
                rangePaint
            )
        }else{
            canvas?.drawLine(
                dxStart + thumbStart.getHalfThumbWidth() + containerLayoutParams.leftMargin,
                height / 2f,
                dxCurrent + thumbEnd.getHalfThumbWidth() + containerLayoutParams.rightMargin,
                height / 2f,
                rangePaint
            )
        }

    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        trackPaint.alpha = if (enabled) 130 else 80
        rangePaint.alpha = if (enabled) 255 else 0
        thumbStart.setOnTouchListener(if (enabled) thumbStartTouch else null)
        thumbStart.isEnabled = enabled

        thumbCurrent.setOnTouchListener(if (enabled) thumbCurrentTouch else null)
        thumbCurrent.isEnabled = enabled

        thumbEnd.setOnTouchListener(if (enabled) thumbEndTouch else null)
        thumbEnd.isEnabled = enabled
    }

    private fun getDeltaX(thumb: Thumb, progress: Float): Float {
        return (container.width - thumb.getThumbWidth()) * (progress / maxProgress)
    }

    private val thumbStartTouch = object : OnTouchListener {
        var startX = 0f
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    thumbStart.isPressed = false
                    v.performClick()
                }
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    startX = event.x - event.rawX + thumbStart.translationX
                    thumbStart.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    var dx = startX + event.rawX
                    dx = max(thumbStart.getHalfThumbWidth().toFloat(), dx)
                    progressStart =
                        (((dx - (thumbStart.getHalfThumbWidth())) / (container.width - thumbStart.getThumbWidth())) * maxProgress)
                    if (progressStart >= progressCurrent) {
                        progressStart = progressCurrent
                    }
                    callback?.onRangeValues(this@PlayerSeekbar, progressStart.toInt(), progressEnd.toInt(), 0)
                    invalidate()
                }
                else -> {
                    return false
                }
            }
            return true
        }
    }

    private val thumbCurrentTouch = object : OnTouchListener {
        var currentX = 0f
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    thumbCurrent.isPressed = false
                    v.performClick()
                    callback?.onStopTrackingTouch()
                }
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    currentX = event.x - event.rawX + thumbCurrent.translationX
                    thumbCurrent.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = currentX + event.rawX
                    progressCurrent = when {
                        dx < thumbStart.x + thumbEnd.getHalfThumbWidth() -> {
                            progressStart
                        }
                        dx > thumbEnd.x + thumbEnd.getHalfThumbWidth() -> {
                            progressEnd
                        }
                        else -> {
                            (((dx - thumbCurrent.getHalfThumbWidth()) / (container.width - thumbCurrent.getThumbWidth())) * maxProgress)
                        }
                    }
                    callback?.onRangeValues(
                        this@PlayerSeekbar,
                        progressStart.toInt(),
                        progressEnd.toInt(),
                        progressCurrent.toInt()
                    )
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
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    thumbEnd.isPressed = false
                    v.performClick()
                }
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    endX = event.x - event.rawX + thumbEnd.translationX
                    thumbEnd.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    var dx = endX + event.rawX
                    dx = min(dx, container.width.toFloat() - thumbEnd.getHalfThumbWidth())
                    progressEnd =
                        (((dx - (thumbEnd.getHalfThumbWidth())) / (container.width - thumbEnd.getThumbWidth())) * maxProgress)
                    if (progressEnd <= progressCurrent) {
                        progressEnd = progressCurrent
                    }
                    callback?.onRangeValues(this@PlayerSeekbar, progressStart.toInt(), progressEnd.toInt(), 0)
                    invalidate()
                }
                else -> {
                    return false
                }
            }
            return true
        }
    }

    fun setStartProgress(progress: Int) {
        progressStart = progress.toFloat()
        invalidate()
    }

    fun setCurrentProgress(progress: Int) {
        if(isRange && progress > progressEnd){
            callback?.onOverflowValue()
        }
        progressCurrent = progress.toFloat()
        invalidate()
    }

    fun setEndProgress(progress: Int) {
        progressEnd = progress.toFloat()
        invalidate()
    }

    fun setMinDifference(difference: Int) {
        minDifference = difference
    }

    fun setRangeMode(isRange: Boolean) {
        this.isRange = isRange
        setThumbVisible()
        invalidate()
    }
    fun setThumbVisible(){
        if(isRange){
            progressStart = progressCurrent
            progressEnd = maxProgress.toFloat()
            thumbStart.visibility = View.VISIBLE
            thumbEnd.visibility = View.VISIBLE
        }else{
            progressStart = 0f
            progressEnd = maxProgress.toFloat()
            thumbStart.visibility = View.INVISIBLE
            thumbEnd.visibility = View.INVISIBLE
        }
    }
    fun setOnRangeSeekBarListener(listener: OnRangeSeekBarListener) {
        callback = listener
    }

    fun getStartProgress(): Int = progressStart.toInt()

    fun getEndProgress(): Int = progressEnd.toInt()

    fun getCurrentProgress(): Int = progressCurrent.toInt()

}

class Thumb(context: Context) : FrameLayout(context) {

    private lateinit var thumb: Rect
    private lateinit var scrubber: View
    private lateinit var disableCircle: Paint
    private var circleRadius: Int = 0

    init {
        init()
    }

    private fun init() {
        setWillNotDraw(false)

        val thumbSize = context.resources.getDimensionPixelSize(R.dimen.thumb_size)
        thumb = Rect(0, 0, thumbSize, thumbSize)
        disableCircle = Paint(Paint.ANTI_ALIAS_FLAG)
        disableCircle.style = Paint.Style.STROKE
        disableCircle.strokeWidth = context.resources.getDimension(R.dimen.line)
        circleRadius = context.resources.getDimensionPixelSize(R.dimen.circle_radius)


        addRipple()
        addScrubber()

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(width / 2f, height / 2f, circleRadius.toFloat(), disableCircle)
    }

    private fun addRipple() {
        val ripple = View(context)
        ripple.setBackgroundResource(getStyledValueFor(context, android.R.attr.selectableItemBackgroundBorderless))
        val rippleSize = context.resources.getDimensionPixelSize(R.dimen.ripple_size)
        val rippleLayoutParams = LayoutParams(rippleSize, rippleSize)
        rippleLayoutParams.gravity = Gravity.CENTER
        addView(ripple, rippleLayoutParams)
    }

    private fun addScrubber() {
        scrubber = View(context)
        val colorAccent = ContextCompat.getColor(context, getStyledValueFor(context, R.attr.colorControlActivated))
        val drawable = getDrawableSupport(context, R.drawable.seekbar_thumb_material_anim)
        val wrapDrawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(wrapDrawable, colorAccent)
        scrubber.background = wrapDrawable
        val scrubberSize = context.resources.getDimensionPixelSize(R.dimen.scrubber_size)
        val scrubberLayoutParams = LayoutParams(scrubberSize, scrubberSize)
        scrubberLayoutParams.gravity = Gravity.CENTER
        addView(scrubber, scrubberLayoutParams)
    }

    fun setColor(color: Int) {
        val drawable = scrubber.background
        val wrapDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrapDrawable, color)
        scrubber.background = wrapDrawable
    }


    fun setDisableCircleColor(color: Int) {
        disableCircle.color = color
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        scrubber.setVisibility(if (enabled) VISIBLE else INVISIBLE)
        disableCircle.alpha = if (enabled) 0 else 255
        invalidate()
    }


    fun getHalfThumbWidth(): Int {
        return thumb.width() / 2
    }


    fun getThumbWidth(): Int {
        return thumb.width()
    }

    private fun getDrawableSupport(context: Context, resId: Int) = context.getDrawable(resId)

    companion object {
        fun getStyledValueFor(context: Context, attr: Int): Int {
            val value = TypedValue()
            context.theme.resolveAttribute(attr, value, true)
            return value.resourceId
        }
    }
}

interface OnRangeSeekBarListener {
    fun onRangeValues(rangeSeekBar: PlayerSeekbar, start: Int, end: Int, current: Int)
    fun onStopTrackingTouch()
    fun onOverflowValue()
}