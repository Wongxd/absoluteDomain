package com.wongxd.absolutedomain.fgt.t192tt

import android.os.Bundle
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.base.aCache.AcacheUtil
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.base.rx.Subscribe
import com.wongxd.absolutedomain.base.rx.ThreadMode
import com.wongxd.absolutedomain.bean.HomeListBean
import com.wongxd.absolutedomain.bean.TypeBean
import com.wongxd.absolutedomain.fgt.BaseTypeFragment
import com.wongxd.absolutedomain.util.JsoupUtil
import com.wongxd.absolutedomain.util.NetworkAvailableUtils
import io.reactivex.Observable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL
import java.util.*

/**
 * Created by wongxd on 2017/11/29.
 */
class t192ttFgt : BaseTypeFragment() {

    @Subscribe(code = RxEventCodeType.SITE_SWITCH, threadMode = ThreadMode.MAIN)
    override fun siteSwitch(url: String) {
        super.siteSwitch(url)
    }

    @Subscribe(code = RxEventCodeType.SYNC_FAVORITE, threadMode = ThreadMode.MAIN)
    override fun doSyncFavorite(p: String) {
        syncFavorite(p)
    }


    override fun doSomethingsWithUrl(url: String, page: Int) {
        val rUrl = getRealPageUrl(url, page)
        Logger.e(rUrl)
        doAsync {
            val s: String
            try {

                if (NetworkAvailableUtils.isNetworkAvailable(activity)) {
                    s = URL(rUrl).readText()
                    AcacheUtil.getDefault(activity, AcacheUtil.StringCache).put(url, s)
                } else s = AcacheUtil.getDefault(activity, AcacheUtil.StringCache).getAsString(url)

                val list = ArrayList<HomeListBean>()
                JsoupUtil.map192TT(s, list)

                uiThread {

                    getList(Observable.create {
                        it.onNext(list)
                        it.onComplete()
                    })
                }
            } catch (e: Exception) {
                uiThread {
                    getList(Observable.create {
                        it.onNext(emptyList())
                        it.onComplete()
                    })
                }
            }


        }


    }


    companion object {
        val typeList = arrayListOf(
                TypeBean("http://www.192tt.com/meitu/xingganmeinv/", "性感"),
                TypeBean("http://www.192tt.com/meitu/siwameitui/", "丝袜"),
                TypeBean("http://www.192tt.com/meitu/weimeixiezhen/", "写真"),
                TypeBean("http://www.192tt.com/meitu/wangluomeinv/", "网络"),
                TypeBean("http://www.192tt.com/meitu/gaoqingmeinv/", "高清"),
                TypeBean("http://www.192tt.com/meitu/motemeinv/", "模特"),
                TypeBean("http://www.192tt.com/meitu/tiyumeinv/", "体育"),
                TypeBean("http://www.192tt.com/meitu/dongmanmeinv/", "动漫")
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        siteSwitch(typeList[0].url)
        RxBus.getDefault().register(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxBus.getDefault().unRegister(this)
    }


}