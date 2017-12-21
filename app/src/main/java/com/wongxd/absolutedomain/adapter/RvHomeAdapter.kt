package com.wongxd.absolutedomain.adapter

import android.graphics.Color
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.bean.HomeListBean
import com.wongxd.absolutedomain.widget.MaskImageView
import com.wongxd.partymanage.base.kotin.extension.loadImg
import com.wongxd.wthing_kotlin.database.Tu
import com.wongxd.wthing_kotlin.database.TuTable
import com.wongxd.wthing_kotlin.database.parseList
import com.wongxd.wthing_kotlin.database.tuDB
import org.jetbrains.anko.db.select

/**
 * Created by wxd1 on 2017/7/10.
 */

class RvHomeAdapter(val click: (HomeListBean) -> Unit) : BaseQuickAdapter<HomeListBean, BaseViewHolder>(R.layout.item_rv_main) {
    override fun convert(helper: BaseViewHolder, item: HomeListBean) {
        with(helper) {
            setText(R.id.tv_title, item.title)
                    .setText(R.id.tv_time, item.date)
            getView<MaskImageView>(R.id.iv).loadImg(item.imgPath)

            mContext.tuDB.use {
                val list = select(TuTable.TABLE_NAME).whereSimple(TuTable.ADDRESS + "=?", item.url).parseList { Tu(HashMap(it)) }
                if (list.isNotEmpty())
                    helper.getView<TextView>(R.id.tv_title).setBackgroundColor(Color.parseColor("#f97198"))
                else
                    helper.getView<TextView>(R.id.tv_title).setBackgroundColor(Color.WHITE)
            }

            itemView.setOnClickListener { click(item) }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder?, position: Int, payloads: MutableList<Any>?) {
        onBindViewHolder(holder,position)
    }

}
