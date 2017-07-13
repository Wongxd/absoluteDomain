package com.wongxd.absolutedomain

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import com.jude.swipbackhelper.SwipeBackHelper
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wongxd.absolutedomain.Retrofit.ApiStore
import com.wongxd.absolutedomain.Retrofit.RetrofitUtils
import com.wongxd.absolutedomain.adapter.RvHomeAdapter
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.bean.HomeListBean
import com.wongxd.absolutedomain.ui.aty.SeePicActivity
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_main.*
import org.jsoup.Jsoup


class AtyMainActivity : BaseSwipeActivity() {

    var currentPage = 1
    var adpater: RvHomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_main)

        //状态栏透明和间距处理
        StatusBarUtil.darkMode(this)
        StatusBarUtil.setPaddingSmart(this, rv_main)
        StatusBarUtil.setPaddingSmart(this, rl_top)
        StatusBarUtil.setPaddingSmart(this, realtime_blur)
        StatusBarUtil.setMargin(this, findViewById(R.id.classic_header))

        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(false)
                .setSwipeRelateEnable(true)


        adpater = RvHomeAdapter {
            val intent = Intent(this, SeePicActivity::class.java)
            intent.putExtra("url", it)
            startActivity(intent)
        }
        rv_main.adapter = adpater
        rv_main.layoutManager = GridLayoutManager(applicationContext, 2)
        rv_main.itemAnimator=LandingAnimator()
//        adpater?.setEmptyView(R.layout.item_rv_empty, rv_main)
//        adpater?.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)

        smartLayout.setOnRefreshListener { doRefresh() }
        smartLayout.setOnLoadmoreListener { doLoadMore(currentPage) }
        tv_about.setOnClickListener { showAbout() }
        initPermission()

        smartLayout.autoRefresh()
    }

    private fun eMailMe() {
        val data = Intent(Intent.ACTION_SENDTO)
        data.data = Uri.parse("mailto:974501076@qq.com")
        data.putExtra(Intent.EXTRA_SUBJECT, "\"绝对领域\" 反馈")
        data.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(data)
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage("数据来源于\n http://www.jdlingyu.moe \n仅供学习交流使用，如果对网站运营带来不便，请联系我（974501076@qq.com）删除。")
                .setNeutralButton("联系我") { dialog, which -> eMailMe() }
                .create()
                .show()
    }

    fun initPermission() {
        val permissions = RxPermissions(this)
        permissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_WIFI_STATE)
                .subscribe { // will emit 2 Permission objects
                    permission ->
                    if (permission.granted) {
                        // `permission.name` is granted !


                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // Denied permission without ask never again
                        val perName = if (permission.name == Manifest.permission.READ_EXTERNAL_STORAGE) "读取存储卡" else "访问wifi状态"
                        TU.cT(perName + " 权限被禁止，无法进行操作")
                    } else {
                        // Denied permission with ask never again
                        // Need to go to the settings
                        val perName = if (permission.name == Manifest.permission.READ_EXTERNAL_STORAGE) "读取存储卡" else "访问wifi状态"
                        val dialog = AlertDialog.Builder(this)
                                .setMessage(perName + "\n权限被禁止，请到 设置-权限 中给予")
                                .setPositiveButton("确定", { dialog1, which ->
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                })
                                .create()
                        dialog.show()
                    }
                }
    }

    fun doRefresh(page: Int = 0) {
        var url = "http://www.jdlingyu.moe/page/$page"
        if (page == 0) {
            currentPage = 1
            url = "http://www.jdlingyu.moe/"
        }
        val apiStore = RetrofitUtils.getStringInstance().create(ApiStore::class.java)
        apiStore.getString(url)
                .subscribeOn(Schedulers.io())
                .map(Function<String, List<HomeListBean>> { s ->
                    val list = ArrayList<HomeListBean>()
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
                    list
                })
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(object : Consumer<List<HomeListBean>> {
                    @Throws(Exception::class)
                    override fun accept(@NonNull t: List<HomeListBean>) {
                        if (t.isNotEmpty()) currentPage++

                        if (page == 0) {
                            smartLayout.finishRefresh()
                            adpater?.setNewData(t)
                        } else {
                            smartLayout.finishLoadmore()
                            adpater?.addData(t)
                        }

                    }
                }, Consumer<Throwable> {
                    TU.cT(it.message)
                    if (page == 0) smartLayout.finishRefresh()
                    else smartLayout.finishLoadmore()
                })
    }

    fun doLoadMore(page: Int) {
        doRefresh(page)
    }


}
