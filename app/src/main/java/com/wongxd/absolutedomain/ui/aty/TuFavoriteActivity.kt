package com.wongxd.absolutedomain.ui.aty

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.partymanage.base.kotin.extension.loadImg
import com.wongxd.wthing_kotlin.database.Tu
import com.wongxd.wthing_kotlin.database.TuTable
import com.wongxd.wthing_kotlin.database.parseList
import com.wongxd.wthing_kotlin.database.tuDB
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_tu_favorite.*
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction

class TuFavoriteActivity : BaseSwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_tu_favorite)
        StatusBarUtil.immersive(this)
        StatusBarUtil.setPaddingSmart(this,rv_favorite)
        StatusBarUtil.setPaddingSmart(this,realtime_blur)
        StatusBarUtil.setPaddingSmart(this,rl_top)
        initRecycle()

        tuDB.use {
            val list = select(TuTable.TABLE_NAME).parseList { (Tu(HashMap(it))) }
            if (list.isNotEmpty()) {
                adpater?.setNewData(list)
                rl_empty.visibility = View.GONE
            }
        }
    }

    var adpater: TuAdapter? = null
    /**
     * recycleView and smartRefreshLayout
     */
    private fun initRecycle() {
        adpater = TuAdapter {
            val intent = Intent(this, SeePicActivity::class.java)
            intent.putExtra("url", it)
            startActivity(intent)
        }

        adpater?.setOnItemLongClickListener { adapter1, view1, position ->
            //收藏
            adpater?.data?.let {
                val item = it[position]
                tuDB.use {
                    transaction {
                        delete(TuTable.TABLE_NAME, TuTable.ADDRESS + "=?", arrayOf(item.address))
                        adpater?.data?.removeAt(position)
                        adpater?.notifyItemRemoved(position)

                    }
                }
            }

            return@setOnItemLongClickListener true
        }



        rv_favorite.adapter = adpater
        rv_favorite.itemAnimator = LandingAnimator()
        rv_favorite.layoutManager = GridLayoutManager(applicationContext, 2)

    }

    override fun finish() {
        RxBus.getDefault().post(RxEventCodeType.SYNC_FAVORITE, SystemClock.currentThreadTimeMillis().toString())
        super.finish()
    }


    class TuAdapter(val click: (String) -> Unit) : BaseQuickAdapter<Tu, BaseViewHolder>(R.layout.item_rv_main) {
        override fun convert(helper: BaseViewHolder?, item: Tu?) {
            helper?.getView<ImageView>(R.id.iv)?.loadImg(item?.imgPath!!)
            helper?.setText(R.id.tv_title, item?.name)
            helper?.itemView?.setOnClickListener { click(item?.address!!) }
        }
    }
}

