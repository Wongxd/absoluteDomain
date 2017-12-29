package com.wongxd.absolutedomain.fgt.mntu92

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
import com.wongxd.absolutedomain.util.NetworkAvailableUtils
import io.reactivex.Observable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import java.net.URL

/**
 * Created by wongxd on 2017/12/29.
 */
class Mntu92Fgt : BaseTypeFragment() {
    @Subscribe(code = RxEventCodeType.SITE_SWITCH, threadMode = ThreadMode.MAIN)
    override fun siteSwitch(url: String) {
        super.siteSwitch(url)
    }

    @Subscribe(code = RxEventCodeType.SYNC_FAVORITE, threadMode = ThreadMode.MAIN)
    override fun doSyncFavorite(p: String) {
        syncFavorite(p)
    }


    override fun doSomethingsWithUrl(url: String, page: Int) {
        val rUrl = if (page == 1) {
            currentUrl
        } else {
            currentUrl + "list_$page.html"
        }
        Logger.e(rUrl)
        doAsync {
            val s: String
            try {
                if (NetworkAvailableUtils.isNetworkAvailable(activity)) {
                    s = URL(rUrl).readText()
                    AcacheUtil.getDefault(activity, AcacheUtil.StringCache).put(url, s)
                } else s = AcacheUtil.getDefault(activity, AcacheUtil.StringCache).getAsString(url)

                val list = ArrayList<HomeListBean>()
                val doc = Jsoup.parse(s)
                val elements = doc.select("#container div.post")
                for (element in elements) {


                    val title = element.select("h2").first().text()

                    val time = element.select("div.pac").first().text()

                    val durl = "http://92mntu.com" + element.select("a:has(img)").attr("href")

                    val imgPreview = "http://92mntu.com" + element.select("img").first().attr("src")

                    val homeListBean = HomeListBean(title, imgPreview, durl, time, "", "")
                    list.add(homeListBean)
                    Logger.e("imgPreview---$imgPreview----title----$title----durl---$durl---time---$time")
                }


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
                TypeBean("http://92mntu.com/qcmn/", "清纯美女"),
                TypeBean("http://92mntu.com/xgmn/", "性感美女"),
                TypeBean("http://92mntu.com/swmt/", "丝袜美腿"),
                TypeBean("http://92mntu.com/rhmn/", "日韩美女"),
                TypeBean("http://92mntu.com/mncm/", "美女车模"),
                TypeBean("http://92mntu.com/mnmx/", "美女明星")
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