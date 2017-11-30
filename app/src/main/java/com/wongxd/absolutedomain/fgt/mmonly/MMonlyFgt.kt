package com.wongxd.absolutedomain.fgt.mmonly

import android.os.Bundle
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.base.rx.Subscribe
import com.wongxd.absolutedomain.base.rx.ThreadMode
import com.wongxd.absolutedomain.bean.HomeListBean
import com.wongxd.absolutedomain.bean.TypeBean
import com.wongxd.absolutedomain.fgt.BaseTypeFragment
import com.wongxd.absolutedomain.util.JsoupUtil
import io.reactivex.Observable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL
import java.util.*

/**
 * Created by wongxd on 2017/11/14.
 *
 * 站点 的分类
 */
class MMonlyFgt : BaseTypeFragment() {


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
            var s: String = ""
            try {
                s = URL(rUrl).readText(charset("GBK"))
                Logger.e(s)
                val list = ArrayList<HomeListBean>()
                JsoupUtil.mapMMonly(s, list)

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
                TypeBean("http://www.mmonly.cc/mmtp/qcmn/list_16_1.html", "清纯"),
                TypeBean("http://www.mmonly.cc/mmtp/ctmn/list_17_1.html", "长腿"),
                TypeBean("http://www.mmonly.cc/mmtp/nymn/list_15_1.htmll", "内衣"),
                TypeBean("http://www.mmonly.cc/mmtp/xgmn/list_10_1.html", "性感"),
                TypeBean("http://www.mmonly.cc/mmtp/hgmn/list_12_1.html", "韩国")
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