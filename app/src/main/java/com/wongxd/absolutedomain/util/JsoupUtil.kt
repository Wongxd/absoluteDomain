package com.wongxd.absolutedomain.util

import android.text.TextUtils
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.bean.ChildDetailBean
import com.wongxd.absolutedomain.bean.HomeListBean
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import java.util.*

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


    /***###############################################################一级页面###############################################################################**/

    /**
     * 匹配 kek1234.net
     */
    fun mapkeke(s: String, list: ArrayList<HomeListBean>) {

        val doc = Jsoup.parse(s)
        val ul = doc.getElementById("msy")
        val lis = ul.getElementsByTag("div")
        var isFirst = true
        for (element in lis) {
            try {
                if (isFirst) {
                    isFirst = false
                    continue
                }
                val preview: String? = element.getElementsByClass("img").first().attr("src")
                val a = element.getElementsByClass("title").first().getElementsByTag("a").first()

                val title = a.attr("title")

                val imgUrl = a.attr("href")

                val date = ""
                val view = ""
                val like = " "
//                Logger.e("$preview    $title  $imgUrl  $date  $view")

                list.add(HomeListBean(title, preview!!, imgUrl, date, view, like))
            } catch(e: Exception) {
                continue
            }
        }

    }

    /**
     * 匹配 Mzitu.com
     */
    fun mapMzitu(s: String, list: ArrayList<HomeListBean>) {
        val doc = Jsoup.parse(s)
        val ul = doc.getElementById("pins")
        val lis = ul.getElementsByTag("li")
        for (element in lis) {
            val preview: String? = element.getElementsByTag("a").first().getElementsByTag("img").first().attr("data-original")

            val title = element.getElementsByTag("a").first().getElementsByTag("img").first().attr("alt")

            val imgUrl = element.getElementsByTag("a").first().attr("href")

            val date = element.getElementsByClass("time").first().text()
            val view = element.getElementsByClass("view").first().text()

            val like = " "
//            Logger .e("$preview  +  $title  +$imgUrl  +$date  +$view")

            list.add(HomeListBean(title, preview!!, imgUrl, date, view, like))
        }

    }

    /**
     * 匹配 jdlingyu.moe
     */
    fun mapJdlingyu(s: String, list: ArrayList<HomeListBean>) {
        val select = Jsoup.parse(s).select("#postlist > div.pin")
        for (element in select) {

            var preview: String? = element.select("div.pin-coat > a > img").attr("original")
            if (preview == null || preview.length < 5) {
                preview = element.select("div.pin-coat > a > img").attr("src")
            }
            val title = element.select("div.pin-coat > a > img").attr("alt")

            val imgUrl = element.select("div.pin-coat > a").attr("href")

            val date = element.select("div.pin-coat > div.pin-data > span.timer > span").text()

            val like = element.select("div.pin-coat > div.pin-data > a.likes > span > span").text()

            val view = element.select("div.pin-coat > div.pin-data > a.viewsButton > span").text()

            list.add(HomeListBean(title, preview!!, imgUrl, date, view, like))
        }
    }


    /**
     * 匹配 192tt
     */
    fun map192TT(s: String, list: ArrayList<HomeListBean>) {
        val select = Jsoup.parse(s).select("body > div.mainer > div.piclist > ul > li")
        for (element in select) {
            val preview = element.select("a > img").attr("lazysrc")

            val imgUrl = element.select("a").attr("href")

            val description = element.select("a > span").text()

            val date = element.select("b.b1").text()

            list.add(HomeListBean(description, preview, imgUrl, date, "", ""))
        }


    }

    /**
     * mmonly 图集列表
     */
    fun mapMMonly(s: String, list: ArrayList<HomeListBean>) {


        val select = Jsoup.parse(s).select("#infinite_scroll > div.item > div.item_t > div > div.ABox > a")

        for (element in select) {


            val date = ""
            val preview = element.select("img").attr("original")

            val url = element.attr("href")

            val description = element.select("img").attr("alt")

            list.add(HomeListBean(description, preview, url, date, "", ""))

        }

    }


    /**#############################################################二级页面#############################################################################**/


    /**
     * 获取 keke1234 某个图集的详情
     *
     * @param url: 图集的url
     *
     */
    fun getkeke1234ChildDetail(url: String): ChildDetailBean? {
        if (TextUtils.isEmpty(url)) return null
        var doc: Document? = null
        var childList: ChildDetailBean? = null
        val urls = ArrayList<String>()
        try {

            doc = Jsoup.parse(URL(url).readText(charset("GBK")))
            val etTitle = doc!!.getElementsByClass("pageheader entrypage").first().getElementsByTag("h2")
            val title = etTitle.text()


            val div = doc.select(".page-list").first()
            val ps = div.select("p")


            ps.mapTo(urls) { it.getElementsByTag("img").first().attr("src") }

            val page = doc.select(".page")
            val current = page.select(".current").first().text()

            getkeke1234Deep(url, current, urls)

            childList = ChildDetailBean(title, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return childList
    }

    /**
     * 获取 keke1234 某个图集的详情
     */
    private fun getkeke1234Deep(path: String, currentPage: String, urls: ArrayList<String>) {
        val pageNo: Int = currentPage.toInt() + 1

        val url = path.replace(".html", "") + "_$pageNo.html"

        try {
            val doc = Jsoup.parse(URL(url).readText(charset("GBK")))
            val div = doc.select(".page-list").first()
            val ps = div.select("p")
            ps.mapTo(urls) { it.getElementsByTag("img").first().attr("src") }
            val page = doc.select(".page")
            val current = page.select(".current").first().text()
            getkeke1234Deep(path, current, urls)
        } catch(e: Exception) {
            return
        }

    }


    /**
     * 获取 jdlingyu 某个图集的详情
     *
     * @param url: 图集的url
     *
     */
    fun getJdlingyuChildDetail(url: String): ChildDetailBean? {
        if (TextUtils.isEmpty(url)) return null
        var doc: Document? = null
        var childList: ChildDetailBean? = null
        try {
            doc = Jsoup.parse(URL(url).readText())
            val etTitle = doc!!.getElementsByClass("main-title").first()
            val title = etTitle.text()
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


    /**
     * 获取 mm131 某个图集的详情
     *
     * @param url: 图集的url
     *
     */
    fun getMM131ChildDetail(url: String): ChildDetailBean? {
        if (TextUtils.isEmpty(url)) return null
        var doc: Document? = null
        var childList: ChildDetailBean? = null
        val urls = ArrayList<String>()
        try {
            doc = Jsoup.parse(URL(url).readText())
            val etTitle = doc!!.getElementsByClass("main-title").first()
            val title = etTitle.text()
            val es_item = doc.getElementsByClass("main-image").first()
            val a = es_item.getElementsByTag("a").first()
            val imgUrl = a.getElementsByTag("img").first().attr("src")
            val href = a.attr("href")
            urls.add(imgUrl)
            if (url.length <= href.length) getMeiziDeep(href, urls)

            childList = ChildDetailBean(title, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return childList
    }

    /**
     * mm131递归调用 爬取
     */
    fun getMM131Deep(url: String, urls: ArrayList<String>) {
        try {
            val doc = Jsoup.parse(URL(url).readText())
            val etTitle = doc!!.getElementsByClass("main-title").first()
            val es_item = doc.getElementsByClass("main-image").first()
            val a = es_item.getElementsByTag("a").first()
            val imgUrl = a.getElementsByTag("img").first().attr("src")
            val href = a.attr("href")
            Logger.e("妹子图 img  " + imgUrl)
            urls.add(imgUrl)
            if (url.length <= href.length) getMeiziDeep(href, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * 获取 mzitu 某个图集的详情
     *
     * @param url: 图集的url
     *
     */
    fun getMeizituChildDetail(url: String): ChildDetailBean? {
        if (TextUtils.isEmpty(url)) return null
        var doc: Document? = null
        var childList: ChildDetailBean? = null
        val urls = ArrayList<String>()
        try {
            doc = Jsoup.parse(URL(url).readText())
            val etTitle = doc!!.getElementsByClass("main-title").first()
            val title = etTitle.text()
            val es_item = doc.getElementsByClass("main-image").first()
            val a = es_item.getElementsByTag("a").first()
            val imgUrl = a.getElementsByTag("img").first().attr("src")
            val href = a.attr("href")
            urls.add(imgUrl)
            if (url.length <= href.length) getMeiziDeep(href, urls)

            childList = ChildDetailBean(title, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return childList
    }

    /**
     * meizi递归调用 爬取
     */
    fun getMeiziDeep(url: String, urls: ArrayList<String>) {
        try {
            val doc = Jsoup.parse(URL(url).readText())
            val etTitle = doc!!.getElementsByClass("main-title").first()
            val es_item = doc.getElementsByClass("main-image").first()
            val a = es_item.getElementsByTag("a").first()
            val imgUrl = a.getElementsByTag("img").first().attr("src")
            val href = a.attr("href")
            Logger.e("妹子图 img  " + imgUrl)
            urls.add(imgUrl)
            if (url.length <= href.length) getMeiziDeep(href, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 获取 192tt 某个图集的详情
     *
     * @param url: 图集的url
     *
     */
    fun get192TTDetail(urlOrigin: String): ChildDetailBean? {
        if (TextUtils.isEmpty(urlOrigin)) return null
        var url = urlOrigin
        var tagUrl = ""
        if (url.contains("_1.html")) {
            url = url.replace("_1.html", ".html")
            tagUrl = url.replace("http://www.192tt.com", "").replace(".html", "")
        }

        if (!url.contains("192tt")) {
            tagUrl = url.replace(".html", "")
            url = "http://www.192tt.com" + url
        } else {
            tagUrl = url.replace(".html", "").replace("http://www.192tt.com", "")
        }

//        Logger.e("$urlOrigin  +  $url   +  $tagUrl"  )
        var doc: Document? = null
        var childList: ChildDetailBean? = null
        val urls = ArrayList<String>()
        try {
            doc = Jsoup.parse(URL(url).readText())
            val imgUrl = doc.select("#p > center > img").first().attr("lazysrc")
            val title = doc.select("#p > center > img").first().attr("alt")
            urls.add(imgUrl)

            var current = doc.select("#nownum").first().text().toInt()
            val total = doc.select("#allnum").first().text().toInt()

            if (current < total) {
                JsoupUtil.get192TTDeep(tagUrl, current + 1, urls)
            }
            childList = ChildDetailBean(title, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return childList
    }

    /**
     * 192tt 递归调用 爬取
     */
    fun get192TTDeep(tagUrl: String, page: Int, urls: ArrayList<String>) {
        try {
            val url = "http://www.192tt.com" + tagUrl + "_$page.html"
            val doc = Jsoup.parse(URL(url).readText())
            val imgUrl = doc.select("#p > center > img").first().attr("lazysrc")
            urls.add(imgUrl)
            val current = doc.select("#nownum").first().text().toInt()
            val total = doc.select("#allnum").first().text().toInt()
            if (current < total) {
                JsoupUtil.get192TTDeep(tagUrl, current + 1, urls)
            }
//            Logger.e("$imgUrl   $current  $total  $tagUrl")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * 获取 mmonly 某个图集的详情
     *
     * @param url: 图集的url
     *
     */
    fun getMMonlyDetail(urlOrigin: String, title: String): ChildDetailBean? {
        if (TextUtils.isEmpty(urlOrigin)) return null
        var url = urlOrigin
        var tagUrl = ""
        if (url.contains("_1.html")) {
            url = url.replace("_1.html", ".html")
            tagUrl = url.replace("http://www.mmonly.cc", "").replace(".html", "")
        }

        if (!url.contains("mmonly")) {
            tagUrl = url.replace(".html", "")
            url = "http://www.mmonly.cc" + url
        } else {
            tagUrl = url.replace(".html", "").replace("http://www.mmonly.cc", "")
        }

//        Logger.e("$urlOrigin  +  $url   +  $tagUrl"  )
        var doc: Document? = null
        var childList: ChildDetailBean? = null
        val urls = ArrayList<String>()
        try {
            val html = URL(url).readText(charset = charset("GBK"))
            doc = Jsoup.parse(html)

            val imgUrl = doc.select("#ArticleBox > p >a >img").first().attr("src")
            urls.add(imgUrl)

            val total = doc.select("#pageAll").text().toInt()
            val current = doc.select("#thisclass").text().toInt()
            Logger.e("$title  $total  $current  $tagUrl")
            if (current < total) {
                JsoupUtil.getMMonlyDeep(tagUrl, current + 1, urls)
            }
            childList = ChildDetailBean(title, urls)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return childList
    }

    /**
     * mmonly 递归调用 爬取
     */
    fun getMMonlyDeep(tagUrl: String, page: Int, urls: ArrayList<String>) {
        try {
            val url = "http://www.mmonly.cc" + tagUrl + "_$page.html"
            val html = URL(url).readText(charset = charset("GBK"))
            val doc = Jsoup.parse(html)
            Logger.e("递归调用 $html ")
            val imgUrl = doc.select("#ArticleBox > p >a >img").first().attr("src")
            urls.add(imgUrl)
            val total = doc.select("#pageAll").text().toInt()
            val current = doc.select("#thisclass").text().toInt()
            if (current < total) {
                JsoupUtil.getMMonlyDeep(tagUrl, current + 1, urls)
            }
            Logger.e("$imgUrl   $current  $total  $tagUrl")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}

