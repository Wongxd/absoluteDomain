package com.wongxd.absolutedomain.ui.aty

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.adapter.RvSeePicAdapter
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.aCache.AcacheUtil
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.base.rx.Subscribe
import com.wongxd.absolutedomain.bean.ChildDetailBean
import com.wongxd.absolutedomain.util.JsoupUtil
import com.wongxd.absolutedomain.util.StatusBarUtil
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
        StatusBarUtil.darkMode(this)
        StatusBarUtil.setPaddingSmart(this, rv_see_pic)
        StatusBarUtil.setPaddingSmart(this, rl_top)
        StatusBarUtil.setPaddingSmart(this, realtime_blur)
        StatusBarUtil.setMargin(this, findViewById(R.id.gifview))
        RxBus.getDefault().register(this)
        adpater = RvSeePicAdapter {
            ViewBigImageActivity.startActivity(this, it.position, adpater?.data as ArrayList<String>?, it.v)
        }

        rv_see_pic.adapter = adpater
        rv_see_pic.layoutManager = LinearLayoutManager(applicationContext)
        adpater?.setEmptyView(R.layout.item_rv_empty, rv_see_pic)
        adpater?.openLoadAnimation(BaseQuickAdapter.SLIDEIN_RIGHT)

        val url = intent.getStringExtra("url")
        smartLayout.setOnRefreshListener { doGetDetail(url) }
        doAsync {
            val childCache: ChildDetailBean? = AcacheUtil.getDefault(applicationContext, AcacheUtil.ObjCache).getAsObject(url) as ChildDetailBean?
            uiThread {
                if (childCache != null && childCache.list.isNotEmpty()) {
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
