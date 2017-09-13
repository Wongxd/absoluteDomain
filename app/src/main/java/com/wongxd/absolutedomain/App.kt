package com.wongxd.absolutedomain

import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreater
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreater
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.wongxd.absolutedomain.base.exception.CrashHandler
import com.wongxd.absolutedomain.util.TU
import me.jessyan.progressmanager.ProgressManager
import okhttp3.OkHttpClient
import kotlin.properties.Delegates


/**
 * Created by wxd1 on 2017/7/10.
 */
class App : SampleApplication() {
    companion object {
        var instance: App by Delegates.notNull()
        //这里我就不写管理类了,捡个懒,直接在 Application 中管理单例 Okhttp
        private var mOkHttpClient: OkHttpClient by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        mOkHttpClient = ProgressManager.getInstance().with(OkHttpClient.Builder())
                .build()

        TU.register(this)
        if (BuildConfig.LOG_DEBUG) {
            Logger.init().logLevel(LogLevel.FULL)
        } else {
            CrashHandler.getInstance().init(this)
            Logger.init().logLevel(LogLevel.NONE)
        }

        //smartRefresh
        SmartRefreshLayout.setDefaultRefreshHeaderCreater(DefaultRefreshHeaderCreater { context, layout -> ClassicsHeader(context) })

        SmartRefreshLayout.setDefaultRefreshFooterCreater(DefaultRefreshFooterCreater { context, layout -> ClassicsFooter(context) })
    }


    fun getOkHttpClient() = mOkHttpClient
}