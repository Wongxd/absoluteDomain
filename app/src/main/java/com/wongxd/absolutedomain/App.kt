package com.wongxd.absolutedomain

import android.app.Application
import com.wongxd.absolutedomain.base.exception.CrashHandler
import com.wongxd.absolutedomain.util.TU
import me.jessyan.progressmanager.ProgressManager
import okhttp3.OkHttpClient
import kotlin.properties.Delegates


/**
 * Created by wxd1 on 2017/7/10.
 */
class App : Application() {
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

        CrashHandler.getInstance().init(this)
    }



    fun getOkHttpClient() = mOkHttpClient
}