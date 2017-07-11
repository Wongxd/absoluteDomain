package com.wongxd.absolutedomain

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jude.swipbackhelper.SwipeBackHelper
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wongxd.absolutedomain.adapter.RvHomeAdapter
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.aCache.AcacheUtil
import com.wongxd.absolutedomain.bean.HomeCacheBean
import com.wongxd.absolutedomain.ui.aty.SeePicActivity
import com.wongxd.absolutedomain.util.JsoupUtil
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import kotlinx.android.synthetic.main.aty_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class AtyMainActivity : BaseSwipeActivity() {

    val lastCache = "lastCache"
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
//        adpater?.setEmptyView(R.layout.item_rv_empty, rv_main)
        adpater?.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)

        smartLayout.setOnRefreshListener { doRefresh() }
        smartLayout.setOnLoadmoreListener { doLoadMore(currentPage) }

        // if can get cache ,use chache else  from net
        doAsync {
            val homeCache: HomeCacheBean? = AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache).getAsObject(lastCache) as HomeCacheBean?
            uiThread {
                if (homeCache?.list?.size != 0) {
                    adpater?.setNewData(homeCache?.list)
                    currentPage++
                } else smartLayout.autoRefresh()
            }
        }

        tv_about.setOnClickListener { showAbout() }

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

    override fun onResume() {
        super.onResume()
        initPermission()
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

    fun doRefresh() {
        currentPage = 1
        doAsync {
            val homeList = JsoupUtil.getList(1)
            if (homeList == null) {
                uiThread {
                    smartLayout.finishRefresh()
                    TU.cT("服务器开小差了")
                    adpater?.setNewData(null)
                }
                return@doAsync
            }
            //put cache
            AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache)
                    .put(lastCache, HomeCacheBean(SystemClock.currentThreadTimeMillis().toString()
                            , homeList))
            uiThread {
                with(homeList) {
                    smartLayout.finishRefresh()
                    if (size != 0) currentPage++
                    adpater?.setNewData(this)
                }
            }
        }
    }

    fun doLoadMore(page: Int) {
        doAsync {
            val homeList = JsoupUtil.getList(page)
            if (homeList == null) {
                uiThread {
                    smartLayout.finishLoadmore()
                    TU.cT("服务器开小差了")
                }
                return@doAsync
            }
            uiThread {
                with(homeList) {
                    smartLayout.finishLoadmore()
                    if (size != 0) currentPage++
                    adpater?.addData(this)
                }
            }
        }
    }


}
