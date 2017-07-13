package com.wongxd.absolutedomain.util

import android.text.TextUtils
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.bean.ChildDetailBean
import com.wongxd.absolutedomain.bean.HomeListBean
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL

object JsoupUtil {

    /**
     * 获取首页的图集列表
     * @param page 页码
     */
    fun getList(page: Int): List<HomeListBean>? {

        val url = if (page <= 1) "http://www.jdlingyu.moe/" else "http://www.jdlingyu.moe/page/" + page
        var doc: Document? = null
        val list = ArrayList<HomeListBean>()
        try {
            doc = Jsoup.parse(URL(url), 8000)
            val es_item = doc!!.getElementsByClass("main-content").first()
            val items = es_item.getElementsByClass("pin-coat")

            for (i in items) {
                //包含 url 和 title 的a标签
                val a = i.getElementsByTag("a").first()
                val title = a.getElementsByClass("bg").text()
                val imgPath = i.getElementsByTag("a").first().getElementsByTag("img").first().attr("original")
                Logger.e(i.getElementsByTag("a").first().getElementsByTag("img").first().toString())

                val childUrl = a.attr("href")

                val time = i.getElementsByClass("pin-data clx").first().getElementsByTag("span").first().getElementsByTag("span").first().text()


                val views = i.getElementsByClass("pin-data clx").first().getElementsByTag("a")[1].getElementsByTag("span").first().text()

                val like = i.getElementsByClass("pin-data clx").first().getElementsByTag("a")[0].getElementsByTag("span").first()
                        .getElementsByTag("span").first().text()
                list.add(HomeListBean(title, imgPath, childUrl, time, views, like))

//                Logger.e("$title  $imgPath  $date")
            }

        } catch (e: Exception) {
            e.printStackTrace()
          return null
        }

        return list
    }


    /**
     * 获取某个图集的详情
     *
     * @param url: 图集的url
     *
     */
    fun getChildDetail(url: String): ChildDetailBean? {
        if (TextUtils.isEmpty(url)) return null
        var doc: Document? = null
        var childList: ChildDetailBean? = null
        try {
            doc = Jsoup.parse(URL(url), 8000)
            val etTitle = doc!!.getElementsByClass("main-title").first()
            val title = etTitle.text()
            //            Logger.e("标题: " + title);
            val es_item = doc.getElementsByClass("main-body").first()
            val `as` = es_item.getElementsByTag("a")
            val urls = `as`.indices
                    .mapNotNull {
                        `as`[it]
                    }
                    .map {
                        it.attr("href")
                    }
            childList = ChildDetailBean(title, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return childList
    }

}
