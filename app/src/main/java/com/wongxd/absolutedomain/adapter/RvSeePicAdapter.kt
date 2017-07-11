package com.wongxd.absolutedomain.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wongxd.absolutedomain.App
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.bean.SeeBigPicBean
import me.jessyan.progressmanager.ProgressListener
import me.jessyan.progressmanager.ProgressManager
import me.jessyan.progressmanager.body.ProgressInfo
import java.lang.Exception

/**
 * Created by wxd1 on 2017/7/10.
 */

class RvSeePicAdapter(val click: (SeeBigPicBean) -> Unit) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_rv_see_pic) {
    override fun convert(helper: BaseViewHolder, item: String) {
        with(helper) {
            val iv: ImageView = getView<ImageView>(R.id.iv)

            Glide.with(App.instance).load(item).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(0.1f)
                    .crossFade(700)
                    .placeholder(R.drawable.placeholder)
                    .fitCenter()
                    .into(iv)
            itemView.setOnClickListener { click(SeeBigPicBean(helper.layoutPosition, getView(R.id.iv))) }

            //下载进度
            val spinner = helper.getView<ProgressBar>(R.id.loading)
            spinner.visibility = View.VISIBLE
            ProgressManager.getInstance().addResponseListener(item, object : ProgressListener {
                override fun onProgress(progressInfo: ProgressInfo?) {
                    if (progressInfo?.isFinish!!) spinner.visibility = View.GONE
                    else spinner.progress = progressInfo.percent
                }

                override fun onError(id: Long, e: Exception?) {
                    spinner.visibility = View.GONE
                }
            })
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




