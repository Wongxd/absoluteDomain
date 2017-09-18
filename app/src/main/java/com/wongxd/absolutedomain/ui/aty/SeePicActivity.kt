package com.wongxd.absolutedomain.ui.aty

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import android.view.View
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
import com.wongxd.absolutedomain.util.JsoupUtil
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import com.wongxd.wthing_kotlin.database.*
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_see_pic.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class SeePicActivity : BaseSwipeActivity() {

    var adpater: RvSeePicAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_see_pic)
        //状态栏透明和间距处理
        StatusBarUtil.immersive(this)
        StatusBarUtil.setPaddingSmart(this, rv_see_pic)
        StatusBarUtil.setPaddingSmart(this, realtime_blur)
        StatusBarUtil.setMargin(this, fl_top)
        RxBus.getDefault().register(this)
        adpater = RvSeePicAdapter {
            ViewBigImageActivity.startActivity(this, it.position, adpater?.data as ArrayList<String>?, it.v)
        }
        adpater?.setEnableLoadMore(false)


        rv_see_pic.adapter = adpater
        rv_see_pic.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                .apply { this.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE }
        rv_see_pic.itemAnimator = LandingAnimator()
        rv_see_pic.addItemDecoration(SGSpacingItemDecoration(3, DensityUtil.dp2px(4f)))

        val url = intent.getStringExtra("url")
        smartLayout.setOnRefreshListener { doGetDetail(url) }
        doAsync {
            val childCache: ChildDetailBean? = AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache).getAsObject(url) as ChildDetailBean?
            uiThread {
                if (childCache != null && childCache.list.isNotEmpty()) {
                    rl_empty.visibility = View.GONE
                    tv_title.text = childCache.title
                    adpater?.setNewData(childCache.list)
                    doFavoriteLogic(url, childCache.title)
                } else smartLayout.autoRefresh()
            }
        }
    }


    fun doGetDetail(url: String) {
        if (TextUtils.isEmpty(url)) {
            TU.cT("没有获取到 该图集 的 url")
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
                    rl_empty.visibility = View.GONE
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
        com.orhanobut.logger.Logger.e(url)
        if (url.contains("jdlingyu."))
            return JsoupUtil.getJdlingyuChildDetail(url)
        else if (url.contains("mm131."))
            return JsoupUtil.getMM131ChildDetail(url)
        else if (url.contains("192tt."))
            return JsoupUtil.get192TTDetail(url)
        else if (url.contains("mmonly.")){
            val title = intent.getStringExtra("title")
            return JsoupUtil.getMMonlyDetail(url,title)
        }else  if (url.contains("keke123.")){
            return JsoupUtil.getkeke1234ChildDetail(url)
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
