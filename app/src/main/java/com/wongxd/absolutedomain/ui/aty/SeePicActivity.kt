package com.wongxd.absolutedomain.ui.aty

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.orhanobut.logger.Logger
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.scwang.smartrefresh.layout.util.DensityUtil
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.adapter.RvSeePicAdapter
import com.wongxd.absolutedomain.adapter.SGSpacingItemDecoration
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.aCache.AcacheUtil
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.base.rx.Subscribe
import com.wongxd.absolutedomain.bean.ChildDetailBean
import com.wongxd.absolutedomain.download.CustomMission
import com.wongxd.absolutedomain.download.DownloadListActivity
import com.wongxd.absolutedomain.util.JsoupUtil
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.wthing_kotlin.database.*
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_see_pic.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zlc.season.rxdownload3.RxDownload


class SeePicActivity : BaseSwipeActivity() {
    var isAddToDownload = false
    var adpater: RvSeePicAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_see_pic)
        //状态栏透明和间距处理
        StatusBarUtil.immersive(this)
        StatusBarUtil.setPaddingSmart(this, rv_see_pic)
        StatusBarUtil.setMargin(this, fl_top)
        RxBus.getDefault().register(this)
        adpater = RvSeePicAdapter {
            ViewBigImageActivity.startActivity(this, it.position, adpater?.data as ArrayList<String>?, it.v)
        }
        adpater?.setEnableLoadMore(false)


        rv_see_pic.adapter = adpater
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                .apply { this.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE }// gapStrategy 解决 RecycleView做瀑布流滚动时，已加载item的位置来回变动

        rv_see_pic.layoutManager = layoutManager

        rv_see_pic.itemAnimator = LandingAnimator()
        rv_see_pic.addItemDecoration(SGSpacingItemDecoration(3, DensityUtil.dp2px(4f)))


//        RecyclerView滑动过程中不断请求layout的Request，不断调整item见的间隙，并且是在item尺寸显示前预处理，因此解决RecyclerView滑动到顶部时仍会出现移动问题
        rv_see_pic.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                layoutManager.invalidateSpanAssignments() //防止第一行到顶部有空白区域
            }

        })

        adpater?.setEmptyView(R.layout.item_rv_empty, rv_see_pic)
        adpater?.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)


        iv_download_favorite.setOnClickListener {
            if (isAddToDownload) {
                startActivity(Intent(this, DownloadListActivity::class.java))
            } else
                QMUIDialog.MessageDialogBuilder(this)
                        .setTitle("提示")
                        .setMessage("要下载本页所有图片吗？")
                        .addAction("下载") { dialog, index -> createTask(); isAddToDownload = true; dialog.dismiss() }
                        .addAction("取消") { dialog, index -> dialog.dismiss() }
                        .show()
        }


        val url = intent.getStringExtra("url")
        smartLayout.setOnRefreshListener { doGetDetail(url) }
        doAsync {
            val childCache: ChildDetailBean? = AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache).getAsObject(url) as ChildDetailBean?
            uiThread {
                if (childCache != null && childCache.list.isNotEmpty()) {
                    tv_title.text = childCache.title
                    adpater?.setNewData(childCache.list)
                    doFavoriteLogic(url, childCache.title)
                } else smartLayout.autoRefresh()
            }
        }
    }

    private fun createTask() {
        adpater?.data?.map {
            val mission = CustomMission(it, tv_title.text.toString(), it)
            RxDownload.create(mission).subscribe()
            RxDownload.start(mission)
        }
    }


    fun doGetDetail(url: String) {
        if (TextUtils.isEmpty(url)) {
            val dia = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            dia.contentText = "没有获取到 该图集 的 url"
            dia.setCancelable(false)
            dia.setCancelClickListener { dia.dismissWithAnimation() }
            dia.show()
            smartLayout.finishRefresh()
            return
        }
        doAsync {
            val list = handleUrlLogic(url)
            if (list != null && list.list.isNotEmpty())
                AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache).put(url, list)
            uiThread {
                smartLayout.finishRefresh()
                if (list != null && list.list.isNotEmpty()) {
                    tv_title.text = list.title
                    adpater?.setNewData(list.list)

                    doFavoriteLogic(url, list.title)
                }
            }
        }
    }

    /**
     * 不同网站 不同逻辑
     */
    private fun handleUrlLogic(url: String): ChildDetailBean? {
        Logger.e(url)
        if (url.contains("jdlingyu."))
            return JsoupUtil.getJdlingyuChildDetail(url)
        else if (url.contains("mm131."))
            return JsoupUtil.getMM131ChildDetail(url)
        else if (url.contains("192tt."))
            return JsoupUtil.get192TTDetail(url)
        else if (url.contains("mmonly.")) {
            val title = intent.getStringExtra("title")
            return JsoupUtil.getMMonlyDetail(url, title)
        } else if (url.contains("keke123.")) {
            return JsoupUtil.getkeke1234ChildDetail(url)
        } else if (url.contains("nvshens.")) {
            return JsoupUtil.getNvShensChildDetail(url)
        } else if (url.contains("meisiguan.")) {
            return JsoupUtil.getMeiSiGuanDetail(url)
        } else if (url.contains("92mntu.")) {
            return JsoupUtil.getMntu92Detail(url)
        }
        return null
    }

    private fun doFavoriteLogic(url: String, name: String) {
        tuDB.use {
            val items = select(TuTable.TABLE_NAME).whereSimple(TuTable.ADDRESS + "=?", url)
                    .parseList({ Tu(HashMap(it)) })
            if (items.isEmpty()) iv_favorite.setImageResource(R.mipmap.star_border)
            else iv_favorite.setImageResource(R.mipmap.star_solid)
        }


        iv_favorite.setOnClickListener {
            tuDB.use {
                transaction {
                    val items = select(TuTable.TABLE_NAME).whereSimple(TuTable.ADDRESS + "=?", url)
                            .parseList({ Tu(HashMap(it)) })

                    if (items.isEmpty()) {  //如果是空的
                        val tu = Tu()
                        tu.address = url
                        tu.name = name
                        tu.imgPath = intent.getStringExtra("imgPath")
                        insert(TuTable.TABLE_NAME, *tu.map.toVarargArray())
                        iv_favorite.setImageResource(R.mipmap.star_solid)
                    } else {
                        delete(TuTable.TABLE_NAME, TuTable.ADDRESS + "=?", arrayOf(url))
                        iv_favorite.setImageResource(R.mipmap.star_border)
                    }

                    RxBus.getDefault().post(RxEventCodeType.SYNC_FAVORITE, SystemClock.currentThreadTimeMillis().toString())
                }
            }
        }
    }

    @Subscribe(code = RxEventCodeType.IMG_LIST_POSTION_CHANGE)
    internal fun goToThisPosition(p: Int?) {
        rv_see_pic.smoothScrollToPosition(p!!)
    }

    override fun onDestroy() {
        RxBus.getDefault().unRegister(this)
        super.onDestroy()
    }
}
