package com.wongxd.wthing_kotlin

import android.content.Context
import android.util.AttributeSet

/**
 * Created by wxd1 on 2017/6/16.
 */
class MarqueeTextView : android.support.v7.widget.AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun isFocused(): Boolean {
        return true
    }

}