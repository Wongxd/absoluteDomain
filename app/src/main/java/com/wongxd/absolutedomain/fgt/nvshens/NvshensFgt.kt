package com.wongxd.absolutedomain.fgt.nvshens

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
class NvshensFgt : BaseTypeFragment() {

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
                s = URL(rUrl).readText()
                val list = ArrayList<HomeListBean>()
                JsoupUtil.mapNvShens(s, list)

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
                TypeBean("https://www.nvshens.com/gallery/yazhou/", "亚洲"),
                TypeBean("https://www.nvshens.com/gallery/rihan/", "日韩"),
                TypeBean("https://www.nvshens.com/gallery/neidi/", "内地"),
                TypeBean("https://www.nvshens.com/gallery/taiwan/", "台湾"),
                TypeBean("https://www.nvshens.com/gallery/xianggang/", "香港"),
                TypeBean("https://www.nvshens.com/gallery/aomen/", "澳门"),
                TypeBean("https://www.nvshens.com/gallery/riben/", "日本"),
                TypeBean("https://www.nvshens.com/gallery/hanguo/", "韩国"),
                TypeBean("https://www.nvshens.com/gallery/malaixiya/", "马来西亚"),
                TypeBean("https://www.nvshens.com/gallery/yuenan/", "越南"),
                TypeBean("https://www.nvshens.com/gallery/taiguo/", "泰国"),
                TypeBean("https://www.nvshens.com/gallery/feilvbin/", "菲律宾"),
                TypeBean("https://www.nvshens.com/gallery/hunxue/", "混血"),
                TypeBean("https://www.nvshens.com/gallery/oumei/", "欧美"),
                TypeBean("https://www.nvshens.com/gallery/yindu/", "印度"),
                TypeBean("https://www.nvshens.com/gallery/feizhou/", "非洲")
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