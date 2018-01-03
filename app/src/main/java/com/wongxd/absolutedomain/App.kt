package com.wongxd.absolutedomain

import android.app.Application
import android.content.Context
import android.os.Environment
import android.support.multidex.MultiDex
import com.luomi.lm.ad.DRAgent
import com.luomi.lm.ad.LogUtil
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreater
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreater
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.wongxd.absolutedomain.base.exception.CrashHandler
import com.wongxd.absolutedomain.bean.UserBean
import com.wongxd.absolutedomain.util.TU
import me.jessyan.progressmanager.ProgressManager
import okhttp3.OkHttpClient
import zlc.season.rxdownload3.core.DownloadConfig
import zlc.season.rxdownload3.extension.ApkInstallExtension
import zlc.season.rxdownload3.http.OkHttpClientFactoryImpl
import java.io.File
import kotlin.properties.Delegates


/**
 * Created by wxd1 on 2017/7/10.
 */
class App : Application() {
    companion object {
        var filePath: String = "/mnt" + File.separator + "download"
        var instance: App by Delegates.notNull()
        //这里我就不写管理类了,捡个懒,直接在 Application 中管理单例 Okhttp
        private var mOkHttpClient: OkHttpClient by Delegates.notNull()
        var user: UserBean? = null
        val BMOB_ID: String = "33c3293abda15ed00bbb74776573e9be"
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this as Context)
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


        val hasSDCard = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        if (hasSDCard) {
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator + getString(R.string.app_name) + File.separator + "download"
        } else
            filePath = "/mnt" + File.separator + getString(R.string.app_name) + File.separator + "download"

        val f = File(filePath)
        if (!f.exists()) f.mkdirs()

        val builder = DownloadConfig.Builder.create(this)
                .setFps(20)                         //设置更新频率
                .enableAutoStart(true)              //自动开始下载
                .setDefaultPath(filePath)     //设置默认的下载地址
                .enableDb(true)                             //启用数据库
//                .setDbActor(CustomSqliteActor(this))        //自定义数据库
                .enableService(true)                        //启用Service
//                .enableNotification(true)                   //启用Notification
//                .setNotificationFactory(NotificationFactoryImpl())        //自定义通知
                .setOkHttpClientFacotry(OkHttpClientFactoryImpl())        //自定义OKHTTP
                .addExtension(ApkInstallExtension::class.java)          //添加扩展

        DownloadConfig.init(builder)

        //广告
        LogUtil.setENABLE_LOGCAT(true)
        DRAgent.getInstance().init(this.applicationContext, "65a3f31939037d2f2329fcf80a1069ca", true)
    }


    fun getOkHttpClient() = mOkHttpClient


}