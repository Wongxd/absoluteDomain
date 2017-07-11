package com.wongxd.partymanage.base.kotin.extension

import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Created by wxd1 on 2017/6/30.
 */

fun ImageView.loadImg(imgPath: Any) {
    Glide.with(context.applicationContext).load(imgPath)
            .into(this)
}