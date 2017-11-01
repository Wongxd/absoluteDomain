package com.wongxd.absolutedomain


import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.view.MenuItem
import android.view.View
import com.jude.swipbackhelper.SwipeBackHelper
import com.orhanobut.logger.Logger
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wongxd.absolutedomain.Retrofit.ApiStore
import com.wongxd.absolutedomain.Retrofit.RetrofitUtils
import com.wongxd.absolutedomain.adapter.RvHomeAdapter
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.base.rx.Subscribe
import com.wongxd.absolutedomain.bean.HomeListBean
import com.wongxd.absolutedomain.ui.aty.SeePicActivity
import com.wongxd.absolutedomain.ui.aty.ThemeActivity
import com.wongxd.absolutedomain.ui.aty.TuFavoriteActivity
import com.wongxd.absolutedomain.util.JsoupUtil
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import com.wongxd.absolutedomain.util.cache.DataCleanManager
import com.wongxd.absolutedomain.util.cache.GlideCatchUtil
import com.wongxd.wthing_kotlin.database.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_main.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import java.net.URL


class AtyMainActivity : BaseSwipeActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_theme -> startActivity(Intent(this, ThemeActivity::class.java))
            R.id.menu_cache -> cacheThing()
            R.id.menu_about -> showAbout()
//            R.id.menu_cache -> unInstallVideoPlugin()
//            R.id.menu_about -> loadVideoPlugin()
            R.id.menu_tu_favorite -> {
                startActivity(Intent(this, TuFavoriteActivity::class.java))
            }

        //右边
            R.id.menu_jdlingyu -> site = 1
            R.id.menu_keke123 -> site = 2
            R.id.menu_192tt -> site = 3
            R.id.menu_mmonly -> site = 4
            R.id.menu_nvshens -> site = 5


        }
        drawerlayout.postDelayed({
            if (drawerlayout.isDrawerOpen(nav_aty_main))
                drawerlayout.closeDrawer(nav_aty_main)
            if (drawerlayout.isDrawerOpen(nav_aty_main_right)) {
                smartLayout.autoRefresh()
                drawerlayout.closeDrawer(nav_aty_main_right)
            }
        }, 500)
        return true
    }

    /**
     * 加载视频播放插件
     */
    //    private fun loadVideoPlugin() {
//
//        if (RePlugin.isPluginInstalled("com.apkfuns.jsbridgesample")) {
//            RePlugin.startActivity(this@AtyMainActivity,
//                    RePlugin.createIntent("com.apkfuns.jsbridgesample", "com.wongxd.jsbridgesample.view.MainActivity"))
//        } else {
//            Toast.makeText(this@AtyMainActivity, "You must install wongxd_video first!", Toast.LENGTH_SHORT).show()
//
//            val path = Environment.getExternalStorageDirectory().path + "/" + packageName + "/plugin/" + "wongxd_plugin_js.apk"
//            val info = RePlugin.install(path)
//            if (info != null) {
//                Logger.e("插件名  " + info.name)
//                RePlugin.startActivity(this@AtyMainActivity,
//                        RePlugin.createIntent(info.name, "com.wongxd.jsbridgesample.view.MainActivity"))
//            } else {
//                Toast.makeText(this@AtyMainActivity, "install external plugin failed", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//
//    }
//
//
//    private fun unInstallVideoPlugin() {
//        val b = RePlugin.uninstall("com.apkfuns.jsbridgesample")
//        if (b) toast("插件卸载成功！")
//        else toast("插件卸载失败！")
//    }

    /**
     * 缓存
     */
    private fun cacheThing() {
        val imgCache = GlideCatchUtil.getInstance().cacheSize
        val totalCache = DataCleanManager.getTotalCacheSize(applicationContext)
        AlertDialog.Builder(this)
                .setTitle("缓存信息")
                .setMessage("图片缓存: $imgCache \n全部缓存: $totalCache")
                .setNeutralButton("清除全部缓存", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        DataCleanManager.clearAllCache(applicationContext)
                        dialog?.dismiss()
                    }

                })
                .setNegativeButton("清除图片缓存", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        GlideCatchUtil.getInstance().clearCacheDiskSelf()
                        dialog?.dismiss()
                    }

                })
                .create()
                .show()
    }

    var currentPage = 1

    var adpater: RvHomeAdapter? = null

    var site = 1 //当前站点标记

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_main)

        RxBus.getDefault().register(this)
        //状态栏透明和间距处理
        StatusBarUtil.immersive(this)
        StatusBarUtil.setPaddingSmart(this, rv_main)
        StatusBarUtil.setPaddingSmart(this, realtime_blur)
        StatusBarUtil.setMargin(this, findViewById(R.id.fl_top))
        StatusBarUtil.setMargin(this, findViewById(R.id.classic_header))


        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(false)
                .setSwipeRelateEnable(true)


        nav_aty_main.setNavigationItemSelectedListener(this)
        nav_aty_main_right.setNavigationItemSelectedListener(this)

        initRecycle()
        initPermission()


        smartLayout.autoRefresh()

        //加载一次我的博客
        val apiStore = RetrofitUtils.getStringInstance().create(ApiStore::class.java)
        apiStore.getString("https://wongxd.github.io")
                .subscribeOn(Schedulers.io())
                .subscribe()


    }

    /**
     * recycleView and smartRefreshLayout
     */
    private fun initRecycle() {
        adpater = RvHomeAdapter {
            val intent = Intent(this, SeePicActivity::class.java)
            intent.putExtra("url", it.url)
            intent.putExtra("imgPath", it.imgPath)
            intent.putExtra("title", it.title)
            startActivity(intent)
        }
        adpater?.setEnableLoadMore(false)

        adpater?.setOnItemLongClickListener { adapter1, view1, position ->
            //收藏
            adpater?.data?.let {
                val bean = it[position]
                tuDB.use {
                    transaction {
                        val items = select(TuTable.TABLE_NAME).whereSimple(TuTable.ADDRESS + "=?", bean.url)
                                .parseList({ Tu(HashMap(it)) })

                        if (items.isEmpty()) {  //如果是空的
                            val tu = Tu()
                            tu.address = bean.url
                            tu.name = bean.title
                            tu.imgPath = bean.imgPath
                            insert(TuTable.TABLE_NAME, *tu.map.toVarargArray())
                            adpater?.notifyItemChanged(position, "1")
                        } else {
                            delete(TuTable.TABLE_NAME, TuTable.ADDRESS + "=?", arrayOf(bean.url))
                            adpater?.notifyItemChanged(position, "1")
                        }
                    }
                }
            }

            return@setOnItemLongClickListener true
        }



        rv_main.adapter = adpater
        rv_main.itemAnimator = LandingAnimator()
        rv_main.layoutManager = GridLayoutManager(applicationContext, 2)

        smartLayout.setOnRefreshListener { doRefresh() }
        smartLayout.setOnLoadmoreListener { doLoadMore(currentPage) }
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
                .setMessage("数据来源于网络，仅供学习交流使用。切勿 违法及商用。对滥用本软件造成的一切后果，请自行承担。\n如有侵权，请联系该网站管理员。")
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

    fun doLoadMore(page: Int) {
        doRefresh(page)
    }

    fun doRefresh(page: Int = 1) {
        when (site) {
            1 -> tv_title.text = "jdlingyu"
            2 -> tv_title.text = "keke123"
            3 -> tv_title.text = "192tt"
            4 -> tv_title.text = "mmonly"
            5 -> tv_title.text = "nvshens"
        }

        //不同网站，不同url
        val url = handleUrlogic(page)
        Logger.e(url)
        val apiStore = RetrofitUtils.getStringInstance().create(ApiStore::class.java)
        val observalble: Observable<String>
        if (url.contains("keke123.")) {
            observalble = Observable.create {
                it.onNext(URL(url).readText(charset("GBK")))
                it.onComplete()
            }
        } else if (url.contains("mmonly.")) {
            observalble = Observable.create {
                it.onNext(URL(url).readText(charset("GBK")))
                it.onComplete()
            }
        } else observalble = apiStore.getString(url)
        observalble.subscribeOn(Schedulers.io())
                .map(Function<String, List<HomeListBean>> { s ->
                    val list = ArrayList<HomeListBean>()
                    //不同网站 不同的匹配规则
                    mapSpecificSite(s, list)
                    list
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Consumer<List<HomeListBean>> {
                    @Throws(Exception::class)
                    override fun accept(@NonNull t: List<HomeListBean>) {
                        if (t.isNotEmpty()) {
                            currentPage++
                        }
                        rl_empty.visibility = View.GONE
                        if (page == 1) {
                            smartLayout.finishRefresh()
                            adpater?.setNewData(t)
                        } else {
                            smartLayout.finishLoadmore()
                            adpater?.addData(t)
                        }

                        if (adpater?.data?.size == 0 || adpater?.data == null) {
                            rl_empty.visibility = View.VISIBLE
                        }

                    }
                }, Consumer<Throwable>
                {
                    TU.cT(it.message.toString() + " ")
                    if (page == 1) smartLayout.finishRefresh()
                    else smartLayout.finishLoadmore()
                    if (adpater?.data?.size == 0 || adpater?.data == null) {
                        rl_empty.visibility = View.VISIBLE
                    }
                })
    }


    /**
     * 不同网站 不同地址
     */
    private fun handleUrlogic(page: Int): String {

        var url = "http://www.jdlingyu.xyz"

        when (site) {

        //keke123
            2 -> url = "http://www.keke123.cc"
        //192tt
            3 -> url = "http://www.192tt.com/new"

            4 -> url = "http://www.mmonly.cc/mmtp"

            5 -> url = "https://www.nvshens.com/gallery"
        }

        //www.keke123.cc/gaoqing/list_5_2.html
        //页面判断
        var suffix = "/page/$page"
        if (page == 1) {
            currentPage = 1
            suffix = ""
        } else if (url.contains("192tt.com")) {
            url = "http://www.192tt.com"
            suffix = "/listinfo-1-$page.html"
            if (currentPage == 2) currentPage++
        } else if (url.contains("mmonly.cc")) {
            suffix = "/list_9_$page.html"
        } else if (url.contains("keke123")) {
            suffix = "/gaoqing/list_5_$page.html"
        } else if (url.contains("nvshens.com")) {
            suffix = "/$page.html"
        }

        return url + suffix
    }

    /**
     * 特定的匹配规则 获取列表
     */
    private fun mapSpecificSite(s: String, list: ArrayList<HomeListBean>) {
        when (site) {
            1 -> JsoupUtil.mapJdlingyu(s, list)
            2 -> JsoupUtil.mapkeke(s, list)
            3 -> JsoupUtil.map192TT(s, list)
            4 -> JsoupUtil.mapMMonly(s, list)
            5 -> JsoupUtil.mapNvShens(s, list)


        }
    }

    @Subscribe(code = RxEventCodeType.SYNC_FAVORITE)
    fun syncFavorite(p: String) {
        adpater?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        RxBus.getDefault().unRegister(this)
        super.onDestroy()
    }


}
