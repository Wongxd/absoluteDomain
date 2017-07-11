package com.wongxd.absolutedomain.util.progress

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule
import com.wongxd.absolutedomain.App
import java.io.InputStream

/**
 * Created by wxd1 on 2017/7/11.
 */
class GlideConfiguration : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {

    }

    override fun registerComponents(context: Context, glide: Glide) {
        val application = context.applicationContext as App
        //Glide 底层默认使用 HttpConnection 进行网络请求,这里替换为 Okhttp 后才能使用进度监测框架,进行 Glide 的加载进度监听
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(application.getOkHttpClient()))
    }
}