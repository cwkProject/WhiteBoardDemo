package com.latitech.sdk.example

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.opengl.EGL14
import android.opengl.EGLSurface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.latitech.sdk.whiteboard.core.WhiteBoardRenderer


/**
 * 白板控件
 *
 * @author 超悟空
 * @version 1.0 2018/3/21
 * @since 1.0 2018/3/21
 */
class WhiteBoardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs) {

    /**
     * 手势输入坐标变换
     */
    private val touchMatrix = Matrix()

    /**
     * 渲染器
     */
    private val renderer = Renderer()

    /**
     * 白板是否已经创建成功
     *
     * @return true表示创建完成
     */
    val isCreated: Boolean
        get() = renderer.isCreated

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        keepScreenOn = true

        holder.addCallback(renderer)
    }

    /**
     * 请求刷新
     */
    fun requestRender() {
        renderer.requestRender()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isCreated || touchMatrix.isIdentity) {
            return false
        }

        event.transform(touchMatrix)

        var process = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> process = renderer.onTouch(event, 0)
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP -> process =
                renderer.onTouch(event, event.actionIndex)
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL -> for (i in 0 until event.pointerCount) {
                if (renderer.onTouch(event, i)) {
                    process = true
                }
            }
        }

        return process
    }

    override fun onDetachedFromWindow() {
        renderer.release()
        super.onDetachedFromWindow()
    }

    /**
     * 渲染器
     */
    private inner class Renderer : WhiteBoardRenderer(), SurfaceHolder.Callback {

        override fun onCreateSurface(
            display: android.opengl.EGLDisplay,
            config: android.opengl.EGLConfig
        ): EGLSurface {
            return EGL14.eglCreateWindowSurface(
                display, config, holder,
                intArrayOf(EGL14.EGL_NONE), 0
            )
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.v(TAG, "surfaceCreated")
            init()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.v(TAG, "surfaceChanged width:$width,height:$height")

            if (width <= 0 || height <= 0) {
                touchMatrix.reset()
            } else {
                touchMatrix.setScale(1f / width, 1f / height)
            }

            boardWidth = width
            boardHeight = height

            resume()

            updateSurface()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            pause()
        }

        /**
         * 处理一个事件输入源
         *
         * @param event        手势输入事件
         * @param pointerIndex 输入源索引
         *
         * @return true表示事件被消耗，false表示事件被忽略
         */
        internal fun onTouch(event: MotionEvent, pointerIndex: Int): Boolean {
            if (pointerIndex < 0) {
                Log.w(TAG, "onTouchEventInput miss pointer.")
                return false
            }

            var nativeAction = -1

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
                    nativeAction = NATIVE_ACTION_DOWN
                MotionEvent.ACTION_MOVE ->
                    nativeAction = NATIVE_ACTION_MOVE
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP ->
                    nativeAction = NATIVE_ACTION_UP
                MotionEvent.ACTION_CANCEL ->
                    nativeAction = NATIVE_ACTION_CANCEL
                else -> Log.w(
                    TAG,
                    "onTouchEventInput not support event action:" + event.actionMasked
                )
            }

            var nativeToolType = -1

            when (event.getToolType(pointerIndex)) {
                MotionEvent.TOOL_TYPE_STYLUS -> nativeToolType = NATIVE_TOOL_TYPE_STYLUS
                MotionEvent.TOOL_TYPE_FINGER -> nativeToolType = NATIVE_TOOL_TYPE_FINGER
                else -> Log.w(
                    TAG,
                    "onTouchEventInput not support input tool:" + event.getToolType(pointerIndex)
                )
            }

            if (nativeAction < 0 || nativeToolType < 0) {
                return false
            }

            val pressure = if (nativeToolType == NATIVE_TOOL_TYPE_STYLUS)
                event.getPressure(pointerIndex)
            else
                1f

            return nativeTouchEventInput(
                nativeToolType,
                nativeAction,
                event.getPointerId(pointerIndex),
                event.getX(pointerIndex),
                event.getY(pointerIndex),
                pressure,
                event.getSize(pointerIndex),
                event.eventTime
            )
        }
    }
}
