package com.wongxd.absolutedomain.base

import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import com.jude.swipbackhelper.SwipeBackHelper
import com.jude.swipbackhelper.SwipeListener

open class BaseSwipeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SwipeBackHelper.onCreate(this)

//        如果需要可在SwipeBackHelper.onCreate()之后进行如下参数设置：
        SwipeBackHelper.getCurrentPage(this)//获取当前页面
                .setSwipeBackEnable(true)//设置是否可滑动
                //                .setSwipeEdge(200)//可滑动的范围。px。200表示为左边200px的屏幕
                .setSwipeEdgePercent(0.05f)//可滑动的范围。百分比。0.2表示为左边20%的屏幕
                .setSwipeSensitivity(0.5f)//对横向滑动手势的敏感程度。0为迟钝 1为敏感
                .setScrimColor(Color.parseColor("#90000000"))//底层阴影颜色
                .setClosePercent(0.8f)//触发关闭Activity百分比
                .setSwipeRelateEnable(true)//是否与下一级activity联动(微信效果)。默认关
                .setSwipeRelateOffset(500)//activity联动时的偏移量。默认500px。
                .setDisallowInterceptTouchEvent(false)//不抢占事件，默认关（事件将先由子View处理再由滑动关闭处理）
                .addListener(object : SwipeListener {//滑动监听

                    override fun onScroll(percent: Float, px: Int) {//滑动的百分比与距离
                    }

                    override fun onEdgeTouch() {//当开始滑动
                    }

                    override fun onScrollToClose() {//当滑动关闭

                    }
                })


    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        SwipeBackHelper.onPostCreate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SwipeBackHelper.onDestroy(this)
    }
}
