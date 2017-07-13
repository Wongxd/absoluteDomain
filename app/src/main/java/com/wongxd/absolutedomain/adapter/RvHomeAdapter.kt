package com.wongxd.absolutedomain.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.bean.HomeListBean
import com.wongxd.partymanage.base.kotin.extension.loadImg

/**
 * Created by wxd1 on 2017/7/10.
 */

class RvHomeAdapter(val click: (String) -> Unit) : BaseQuickAdapter<HomeListBean, BaseViewHolder>(R.layout.item_rv_main) {
    override fun convert(helper: BaseViewHolder, item: HomeListBean) {
        with(helper) {
            setText(R.id.tv_title, item.title)
                    .setText(R.id.tv_time, item.date)
                    .setText(R.id.tv_view, item.view)
                    .setText(R.id.tv_like, item.like+"次喜欢")
                    .setVisible(R.id.tv_like,false)
            getView<ImageView>(R.id.iv).loadImg(item.imgPath)

//            Logger.e("标题 "+item.title +" 时间 "+item.date +" 浏览 "+item.view+" 喜欢 "+item.like+" 图片地址 "+item.imgPath)
            itemView.setOnClickListener {click(item.url) }
        }
    }

}
