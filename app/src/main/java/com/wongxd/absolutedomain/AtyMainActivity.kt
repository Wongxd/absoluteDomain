package com.wongxd.absolutedomain


import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import com.jude.swipbackhelper.SwipeBackHelper
import com.orhanobut.logger.Logger
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wongxd.absolutedomain.Retrofit.ApiStore
import com.wongxd.absolutedomain.Retrofit.RetrofitUtils
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.bean.TypeBean
import com.wongxd.absolutedomain.fgt.BaseTypeFragment
import com.wongxd.absolutedomain.fgt.jdlingyu.JdlingyuFgt
import com.wongxd.absolutedomain.fgt.keke123.KeKe123Fgt
import com.wongxd.absolutedomain.fgt.mmonly.MMonlyFgt
import com.wongxd.absolutedomain.fgt.nvshens.NvshensFgt
import com.wongxd.absolutedomain.fgt.t192tt.t192ttFgt
import com.wongxd.absolutedomain.ui.aty.ThemeActivity
import com.wongxd.absolutedomain.ui.aty.TuFavoriteActivity
import com.wongxd.absolutedomain.util.TU
import com.wongxd.absolutedomain.util.cache.DataCleanManager
import com.wongxd.absolutedomain.util.cache.GlideCatchUtil
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.aty_main.*
import java.net.URL


class AtyMainActivity : BaseSwipeActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_theme -> startActivity(Intent(this, ThemeActivity::class.java))
            R.id.menu_cache -> cacheThing()
            R.id.menu_about -> showAbout()
            R.id.menu_tu_favorite -> startActivity(Intent(this, TuFavoriteActivity::class.java))


        //右边
            R.id.menu_jdlingyu -> switchSite(1)
            R.id.menu_keke123 -> switchSite(2)
            R.id.menu_192tt -> switchSite(3)
//            R.id.menu_mmonly -> switchSite(4)
            R.id.menu_nvshens -> switchSite(5)


        }


        drawerlayout.postDelayed({
            if (drawerlayout.isDrawerOpen(nav_aty_main))
                drawerlayout.closeDrawer(nav_aty_main)
            if (drawerlayout.isDrawerOpen(nav_aty_main_right)) {
                drawerlayout.closeDrawer(nav_aty_main_right)
            }
        }, 500)

        return true
    }


    private lateinit var currentFgt: BaseTypeFragment

    private fun switchSite(flag: Int) {

        currentFgt.onDestroyView()
        currentFgt.onDetach()

        when (flag) {
            1 -> {
                tv_title.text = "jdlingyu"
                currentTypeList = JdlingyuFgt.typeList
                currentFgt = JdlingyuFgt()
            }
            2 -> {
                tv_title.text = "keke123"
                currentTypeList = KeKe123Fgt.typeList
                currentFgt = KeKe123Fgt()
            }
            3 -> {
                tv_title.text = "192tt"
                currentTypeList = t192ttFgt.typeList
                currentFgt = t192ttFgt()
            }
            4 -> {

                tv_title.text = "mmonly"
                currentTypeList = MMonlyFgt.typeList
                currentFgt = MMonlyFgt()
            }
            5 -> {
                tv_title.text = "nvshens"
                currentTypeList = NvshensFgt.typeList
                currentFgt = NvshensFgt()
            }

        }

        initTablayout()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container_aty_main, currentFgt)
                .commitNow()
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


    override fun onDestroy() {
        currentFgt.onDestroy()
        RxBus.getDefault().unRegister(this)
        super.onDestroy()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_main)

        RxBus.getDefault().register(this)
        //状态栏透明和间距处理


        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(false)
                .setSwipeRelateEnable(true)

        tv_menu_aty_main.setOnClickListener {
            if (drawerlayout.isDrawerOpen(nav_aty_main))
                drawerlayout.closeDrawer(nav_aty_main)
            else
                drawerlayout.openDrawer(nav_aty_main)
        }


        tv_switch_aty_main.setOnClickListener {
            if (drawerlayout.isDrawerOpen(nav_aty_main_right))
                drawerlayout.closeDrawer(nav_aty_main_right)
            else
                drawerlayout.openDrawer(nav_aty_main_right)

        }


        nav_aty_main.setNavigationItemSelectedListener(this)
        nav_aty_main_right.setNavigationItemSelectedListener(this)

        initPermission()

        //加载一次我的博客
        val apiStore = RetrofitUtils.getStringInstance().create(ApiStore::class.java)
        apiStore.getString("https://wongxd.github.io/")
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Logger.e("加载博客 ${it.substring(0, 100)}")
                })


        Thread({ URL("https://wongxd.github.io").readText() }).start()

        currentTypeList = JdlingyuFgt.typeList
        initTablayout()
        currentFgt = JdlingyuFgt()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container_aty_main, currentFgt)
                .commit()


    }


    lateinit var currentTypeList: ArrayList<TypeBean>

    fun initTablayout() {
        tablayout_main.removeAllTabs()
        for (t in currentTypeList) {
            val tab = tablayout_main.newTab()
            tab.text = t.title
            tab.tag = t.url

            tablayout_main.addTab(tab)
        }

        tablayout_main.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val url = tab?.tag as String
                RxBus.getDefault().post(RxEventCodeType.SITE_SWITCH, url)
//                Logger.e("发送具体分类的url地址--$url")
            }

        })
    }


}
