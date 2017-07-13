package com.wongxd.absolutedomain.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wongxd.absolutedomain.App
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.bean.SeeBigPicBean

/**
 * Created by wxd1 on 2017/7/10.
 */

class RvSeePicAdapter(val click: (SeeBigPicBean) -> Unit) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_rv_see_pic) {
    override fun convert(helper: BaseViewHolder, item: String) {
        with(helper) {
            val iv: ImageView = getView<ImageView>(R.id.iv)

            Glide.with(App.instance).load(item)
                    .crossFade(700)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(iv)
            itemView.setOnClickListener { click(SeeBigPicBean(helper.layoutPosition, getView(R.id.iv))) }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder?, position: Int, payloads: MutableList<Any>?) {
        if (payloads == null || payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        convert(holder!!, mData[holder.layoutPosition - headerLayoutCount])
    }

}




