package com.wongxd.absolutedomain.fgt.meisiguan

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
 * Created by wongxd on 2017/12/28.
 */
class MeisiGuanFgt : BaseTypeFragment() {
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
//                JsoupUtil.mapkeke(s, list)
                val doc = Jsoup.parse(s)
                val ul = doc.getElementsByClass("update_area_lists cl").first()
                val lis = ul.getElementsByClass("i_list list_n1")

                for (li in lis) {
                    val img = li.getElementsByClass("waitpic")
                    val imgPreview = img.attr("data-original")
                    val title = img.attr("alt")

                    val a = li.getElementsByTag("a")
                    val durl = a.attr("href")


                   val time = li.getElementsByClass("meta-post").text()

                    val homeListBean = HomeListBean(title,imgPreview,durl,time,"","")
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
                TypeBean("http://www.meisiguan.com/", "时间排序")
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