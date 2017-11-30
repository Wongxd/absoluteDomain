package com.wongxd.partymanage.base.kotin.extension

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.wongxd.absolutedomain.R

/**
 * Created by wxd1 on 2017/6/30.
 */

fun ImageView.loadImg(imgPath: String) {
//    doAsync {
//        val bytes = URL(imgPath).readBytes()
//        uiThread {
//            Glide.with(context.applicationContext).load(bytes)
//                    .placeholder(R.drawable.placeholder)
//                    .error(R.drawable.error)
//                    .crossFade(500)
//                    .into(it)
//        }
//    }

    loadImgByPath(imgPath)

}

fun ImageView.loadImgByPath(imgPath: String) {
    Glide.with(context.applicationContext).load(imgPath)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .crossFade(500)
            .into(this)
}