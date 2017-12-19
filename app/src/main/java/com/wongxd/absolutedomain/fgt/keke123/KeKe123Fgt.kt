package com.wongxd.absolutedomain.fgt.keke123

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

/**
 * Created by wongxd on 2017/11/14.
 *
 * 站点 的分类
 */
class KeKe123Fgt : BaseTypeFragment() {


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
                    s = URL(rUrl).readText(charset("GBK"))
                    AcacheUtil.getDefault(activity, AcacheUtil.StringCache).put(url, s)
                } else s = AcacheUtil.getDefault(activity, AcacheUtil.StringCache).getAsString(url)

                val list = ArrayList<HomeListBean>()
                JsoupUtil.mapkeke(s, list)

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
                TypeBean("http://www.keke123.cc/gaoqing/cn/list_1_1.html", "国产"),
                TypeBean("http://www.keke123.cc/gaoqing/rihan/list_2_1.html", "日韩"),
                TypeBean("http://www.keke123.cc/gaoqing/oumei/list_3_1.html", "欧美")
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