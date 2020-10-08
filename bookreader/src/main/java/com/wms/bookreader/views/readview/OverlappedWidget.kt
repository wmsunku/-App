package com.wms.bookreader.views.readview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Region
import android.graphics.drawable.GradientDrawable
import com.wms.bookreader.views.readview.bean.BookMixAToc
import com.wms.bookreader.views.readview.manager.SettingManager
import com.wms.bookreader.views.readview.manager.ThemeManager


open class OverlappedWidget(context: Context, bookId: String,
                            chaptersList: List<BookMixAToc.MixToc.Chapters>,
                            listener: OnReadStateChangeListener) : BaseReadView(context, bookId, chaptersList, listener) {

    private val mPath0: Path

    private var mBackShadowDrawableLR: GradientDrawable
    private var mBackShadowDrawableRL: GradientDrawable

    init {
        mTouch.x = 0.01f
        mTouch.y = 0.01f

        mPath0 = Path()

        val mBackShadowColors = intArrayOf(-0x5599999a, 0x666666)
        mBackShadowDrawableRL = GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors)
        mBackShadowDrawableRL.gradientType = GradientDrawable.LINEAR_GRADIENT

        mBackShadowDrawableLR = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors)
        mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
    }

    override fun drawCurrentPageArea(canvas: Canvas) {
        mPath0.reset()

        canvas.save()
        if (actiondownX > mScreenWidth shr 1) {
            mPath0.moveTo(mScreenWidth + touch_down, 0f)
            mPath0.lineTo(mScreenWidth + touch_down, mScreenHeight.toFloat())
            mPath0.lineTo(mScreenWidth.toFloat(), mScreenHeight.toFloat())
            mPath0.lineTo(mScreenWidth.toFloat(), 0f)
            mPath0.lineTo(mScreenWidth + touch_down, 0f)
            mPath0.close()
            canvas.clipPath(mPath0, Region.Op.XOR)
            canvas.drawBitmap(mCurPageBitmap!!, touch_down, 0f, null)
        } else {
            mPath0.moveTo(touch_down, 0f)
            mPath0.lineTo(touch_down, mScreenHeight.toFloat())
            mPath0.lineTo(mScreenWidth.toFloat(), mScreenHeight.toFloat())
            mPath0.lineTo(mScreenWidth.toFloat(), 0f)
            mPath0.lineTo(touch_down, 0f)
            mPath0.close()
            canvas.clipPath(mPath0)
            canvas.drawBitmap(mCurPageBitmap!!, touch_down, 0f, null)
        }
        try {
            canvas.restore()
        } catch (e: Exception) {

        }

    }

    override fun drawCurrentPageShadow(canvas: Canvas) {
        canvas.save()
        val shadow: GradientDrawable
        if (actiondownX > mScreenWidth shr 1) {
            shadow = mBackShadowDrawableLR
            shadow.setBounds((mScreenWidth + touch_down - 5).toInt(), 0, (mScreenWidth.toFloat() + touch_down + 5f).toInt(), mScreenHeight)

        } else {
            shadow = mBackShadowDrawableRL
            shadow.setBounds((touch_down - 5).toInt(), 0, (touch_down + 5).toInt(), mScreenHeight)
        }
        shadow.draw(canvas)
        try {
            canvas.restore()
        } catch (e: Exception) {

        }

    }

    override fun drawCurrentBackArea(canvas: Canvas) {
        // none
    }

    override fun drawNextPageAreaAndShadow(canvas: Canvas) {
        canvas.save()
        if (actiondownX > mScreenWidth shr 1) {
            canvas.clipPath(mPath0)
            canvas.drawBitmap(mNextPageBitmap!!, 0f, 0f, null)
        } else {
            canvas.clipPath(mPath0, Region.Op.XOR)
            canvas.drawBitmap(mNextPageBitmap!!, 0f, 0f, null)
        }
        try {
            canvas.restore()
        } catch (e: Exception) {

        }

    }

    override fun calcPoints() {

    }

    override fun calcCornerXY(x: Float, y: Float) {

    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            val x = mScroller.currX.toFloat()
            val y = mScroller.currY.toFloat()
            if (actiondownX > mScreenWidth shr 1) {
                touch_down = -(mScreenWidth - x)
            } else {
                touch_down = x
            }
            mTouch.y = y
            postInvalidate()
        }
    }

    override fun startAnimation() {
        val dx: Int
        if (actiondownX > mScreenWidth / 2) {
            dx = (-(mScreenWidth + touch_down)).toInt()
            mScroller.startScroll((mScreenWidth + touch_down).toInt(), mTouch.y.toInt(), dx, 0, 700)
        } else {
            dx = (mScreenWidth - touch_down).toInt()
            mScroller.startScroll(touch_down.toInt(), mTouch.y.toInt(), dx, 0, 700)
        }
    }

    override fun abortAnimation() {
        if (!mScroller.isFinished) {
            mScroller.abortAnimation()
        }
    }

    override fun restoreAnimation() {
        val dx: Int
        if (actiondownX > mScreenWidth / 2) {
            dx = (mScreenWidth - mTouch.x).toInt()
        } else {
            dx = (-mTouch.x).toInt()
        }
        mScroller.startScroll(mTouch.x.toInt(), mTouch.y.toInt(), dx, 0, 300)
    }

    public override fun setBitmaps(bm1: Bitmap?, bm2: Bitmap?) {
        mCurPageBitmap = bm1
        mNextPageBitmap = bm2
    }

    @Synchronized
    override fun setTheme(theme: Int) {
        resetTouchPoint()
        val bg = ThemeManager.getThemeDrawable(theme)
        if (bg != null) {
            pagefactory!!.setBgBitmap(bg)
            if (isPrepared) {
                pagefactory!!.onDraw(mCurrentPageCanvas)
                pagefactory!!.onDraw(mNextPageCanvas)
                postInvalidate()
            }
        }
        if (theme < 5) {
            SettingManager.Instance().saveReadTheme(theme)
        }
    }
}
