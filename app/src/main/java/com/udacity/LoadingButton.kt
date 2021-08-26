package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val ANIMATION_DURATION = 3000L
        private const val ARC_ANIMATION_LOADING_VALUE = 360f
        private const val LOADING_PROGRESS_SIZE = 0.5
        private const val END_OFFSET_OF_PROGRESS = 120f
    }

    private var widthSize = 0
    private var heightSize = 0
    private val HALF_OF = 2

    private var animatorSet: AnimatorSet = AnimatorSet().apply {
        duration = ANIMATION_DURATION
        disableInteractionWhenLoading(this@LoadingButton)
    }
    private var loadingRectF = RectF()
    private lateinit var buttonBoundRect: Rect
    private var loadingSize = 0f

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    // define paint object for buttonText
    private val buttonTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 50f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var defaultButtonText: CharSequence = ""
    private var buttonText: CharSequence = ""
    private var downloadingButtonText: CharSequence = ""
    private var buttonTextColor = Color.BLACK
    private var progressColor = Color.BLACK
    private var defaultButtonBackgroundColor = Color.BLACK
    private var progressButtonBackgroundColor = Color.GRAY


    private var overlayButtonBackgroundAnimationValue = 0f
    private var loadingProgressAnimationValue = 0f

    private val progressLoadingAnimator =
        ValueAnimator.ofFloat(0f, ARC_ANIMATION_LOADING_VALUE).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                loadingProgressAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }


    private lateinit var buttonOverlayValueAnimator: ValueAnimator

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                buttonText = downloadingButtonText
                if (!::buttonBoundRect.isInitialized) {
                    buttonBoundRect = Rect()
                    buttonTextPaint.getTextBounds(
                        buttonText.toString(),
                        0,
                        buttonText.toString().length,
                        buttonBoundRect
                    )
                }
                startAnimationTogether()
                invalidate()
            }
            ButtonState.Completed -> {
                overlayButtonBackgroundAnimationValue = 0f
                loadingProgressAnimationValue = 0f
                buttonText = defaultButtonText
                animatorSet.cancel()
                invalidate()
            }
            else -> {
                buttonText = defaultButtonText
                invalidate()
            }
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            defaultButtonText = getText(R.styleable.LoadingButton_defaultButtonText)
            downloadingButtonText = getText(R.styleable.LoadingButton_downloadingButtonText)
            buttonTextColor = getColor(R.styleable.LoadingButton_buttonTextColor, Color.BLACK)
            progressColor = getColor(R.styleable.LoadingButton_progressColor, Color.BLACK)
            defaultButtonBackgroundColor =
                getColor(R.styleable
                    .LoadingButton_defaultButtonBackgroundColor, Color.BLACK)
            progressButtonBackgroundColor =
                getColor(R.styleable.LoadingButton_progressBackgroundColor, Color.GRAY)
            buttonText = defaultButtonText
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    private fun startAnimationTogether() {
        animatorSet.apply {
            playTogether(progressLoadingAnimator, buttonOverlayValueAnimator)
        }.start()
    }

    private fun Canvas.drawDefaultStateButton() = buttonPaint.apply {
        color = progressButtonBackgroundColor
    }.run {
        drawRect(
            overlayButtonBackgroundAnimationValue,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(),
            buttonPaint
        )
    }

    private fun drawButtonText(canvas: Canvas) {
        buttonTextPaint.color = buttonTextColor
        canvas.apply {
            val horizontalCenter = (widthSize / HALF_OF).toFloat()
            val verticalCenter =
                (heightSize / 2 - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2)
            drawText(buttonText.toString(), horizontalCenter, verticalCenter, buttonTextPaint)
        }
    }

    private fun drawButton(canvas: Canvas) {
        when (buttonState) {
            ButtonState.Loading -> {
                canvas.drawDownloadingStateButton()
                canvas.drawDefaultStateButton()
                canvas.drawLoadingProgress()
            }
            else -> canvas.drawColor(defaultButtonBackgroundColor)
        }
    }

    private fun Canvas.drawDownloadingStateButton() = buttonPaint.apply {
        color = defaultButtonBackgroundColor
    }.run {
        drawRect(
            0f,
            0f,
            overlayButtonBackgroundAnimationValue,
            heightSize.toFloat(),
            buttonPaint
        )
    }

    private fun Canvas.drawLoadingProgress() {
        progressPaint.color = progressColor
        val endOfInset = END_OFFSET_OF_PROGRESS
        val horizontal =
            (buttonBoundRect.right + buttonBoundRect.width() + loadingSize + endOfInset)
        val vertical = (heightSize / HALF_OF)
        loadingRectF.set(
            RectF(
                horizontal - loadingSize,
                vertical - loadingSize,
                horizontal + loadingSize,
                vertical + loadingSize
            )
        )
        drawArc(
            loadingRectF,
            0f, loadingProgressAnimationValue, true, progressPaint
        )
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawButton(it)
            drawButtonText(it)
        }
    }

    private fun initButtonAnimator() {
        buttonOverlayValueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                overlayButtonBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initButtonAnimator()
        calculateLoadingBarSize(w, h)
    }


    private fun calculateLoadingBarSize(width: Int, height: Int) {
        loadingSize = ((min(width, height) / HALF_OF) * LOADING_PROGRESS_SIZE).toFloat()
    }

    fun updateButtonState(state: ButtonState) {
        buttonState = state
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}