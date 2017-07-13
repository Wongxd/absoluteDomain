package com.wongxd.absolutedomain.ui.aty

import android.os.Bundle
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
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_see_pic.*
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
        StatusBarUtil.setPaddingSmart(this, rl_top)
        StatusBarUtil.setPaddingSmart(this, realtime_blur)
        StatusBarUtil.setMargin(this, findViewById(R.id.gifview))
        RxBus.getDefault().register(this)
        adpater = RvSeePicAdapter {
            ViewBigImageActivity.startActivity(this, it.position, adpater?.data as ArrayList<String>?, it.v)
        }
        adpater?.setEnableLoadMore(false)



        rv_see_pic.adapter = adpater
        rv_see_pic.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                .apply { this.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE }
        rv_see_pic.itemAnimator = LandingAnimator()
        rv_see_pic.addItemDecoration(SGSpacingItemDecoration(2, DensityUtil.dp2px(4f)))

        val url = intent.getStringExtra("url")
        smartLayout.setOnRefreshListener { doGetDetail(url) }
        doAsync {
            val childCache: ChildDetailBean? = AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache).getAsObject(url) as ChildDetailBean?
            uiThread {
                if (childCache != null && childCache.list.isNotEmpty()) {
                    rl_empty.visibility = View.GONE
                    tv_title.text = childCache.title
                    adpater?.setNewData(childCache.list)
                } else smartLayout.autoRefresh()
            }
        }
    }


    fun doGetDetail(url: String) {
        if (TextUtils.isEmpty(url)) return
        doAsync {
            val list = JsoupUtil.getChildDetail(url)
            if (list != null && list.list.isNotEmpty())
                AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache).put(url, list)
            uiThread {
                smartLayout.finishRefresh()
                if (list != null && list.list.isNotEmpty()) {
                    rl_empty.visibility = View.GONE
                    tv_title.text = list.title
                    adpater?.setNewData(list.list)
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
