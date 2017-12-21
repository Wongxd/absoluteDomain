package com.wongxd.absolutedomain.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Created by wongxd on 2017/9/20.
 *
 * 可以在 非pressed 下显示 半透明遮罩
 * 设置了setClickable(true) 或者 设置了 setOnClickListener 才会生效
 */
class MaskImageView : ImageView {

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attr: AttributeSet?) : this(context, attr, 0)

    constructor(context: Context?, attr: AttributeSet?, defStyleAttr: Int) : super(context, attr, defStyleAttr)


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //默认有个半透明的遮罩
        if (!isPressed)
            canvas?.drawColor(0x33000000)
    }

    override fun dispatchSetPressed(pressed: Boolean) {
        super.dispatchSetPressed(pressed)

        //Android中实现view的更新有两组方法，
        // 一组是invalidate，另一组是postInvalidate，
        // 其中前者是在UI线程自身中使用，而后者在非UI线程中使用。
        postInvalidate()
    }

}