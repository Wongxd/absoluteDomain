package com.wongxd.partymanage.base.kotin.extension

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.util.GlideCircleTransform

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
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)//让Glide既缓存全尺寸图片，下次在任何ImageView中加载图片的时候，全尺寸的图片将从缓存中取出，重新调整大小，然后缓存
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
            .diskCacheStrategy(DiskCacheStrategy.ALL)//让Glide既缓存全尺寸图片，下次在任何ImageView中加载图片的时候，全尺寸的图片将从缓存中取出，重新调整大小，然后缓存
            .crossFade(500)
            .into(this)
}


fun ImageView.loadHeader(imgPath: Any) {
    Glide.with(context.applicationContext).load(imgPath)
            .placeholder(R.drawable.not_login_img)
            .crossFade(500)
            .diskCacheStrategy(DiskCacheStrategy.ALL)//让Glide既缓存全尺寸图片，下次在任何ImageView中加载图片的时候，全尺寸的图片将从缓存中取出，重新调整大小，然后缓存
            .transform(GlideCircleTransform(context.applicationContext))
            .into(this)
}