package com.wongxd.absolutedomain.fgt.jdlingyu

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
class JdlingyuFgt : BaseTypeFragment() {

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
                JsoupUtil.mapJdlingyu(s, list)

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
                TypeBean("http://www.jdlingyu.fun/%e8%83%96%e6%ac%a1/", "胖次"),
                TypeBean("http://www.jdlingyu.fun/%e7%89%b9%e7%82%b9/%e4%b8%9d%e8%a2%9c/", "丝袜"),
                TypeBean("http://www.jdlingyu.fun/%e7%89%b9%e7%82%b9/%e6%b1%89%e6%9c%8d/", "汉服"),
                TypeBean("http://www.jdlingyu.fun/%e7%89%b9%e7%82%b9/%e6%ad%bb%e5%ba%93%e6%b0%b4/", "死库水"),
                TypeBean("http://www.jdlingyu.fun/%e7%89%b9%e7%82%b9/%e4%bd%93%e6%93%8d%e6%9c%8d/", "体操服"),
                TypeBean("http://www.jdlingyu.fun/%e7%89%b9%e7%82%b9/%e5%a5%b3%e4%bb%86%e8%a3%85/", "女仆装"),
                TypeBean("http://www.jdlingyu.fun/%e7%89%b9%e7%82%b9/%e6%b0%b4%e6%89%8b%e6%9c%8d/", "水手服&JK"),
                TypeBean("http://www.jdlingyu.fun/%e7%89%b9%e7%82%b9/%e5%92%8c%e6%9c%8d%e6%b5%b4%e8%a1%a3/", "和服&浴衣")
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