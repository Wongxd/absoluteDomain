package com.wongxd.absolutedomain.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.wongxd.absolutedomain.base.exception.AppManager
import com.wongxd.absolutedomain.util.ConfigUtils

/**
 * Created by wxd1 on 2017/7/13.
 */
open class BaseActivity : AppCompatActivity() {
    fun setTheme() {
        val theme = ConfigUtils.getTheme(this)
        this.setTheme(theme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        AppManager.getAppManager().addActivity(this)
    }

    override fun onDestroy() {
        AppManager.getAppManager().removeActivity(this)
        super.onDestroy()
    }
}