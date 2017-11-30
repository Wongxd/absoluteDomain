package com.wongxd.absolutedomain.bean

import android.widget.ImageView
import java.io.Serializable
import java.util.*

/**
 * Created by wxd1 on 2017/7/10.
 */



data class TypeBean(val url: String, val title: String)

data class HomeListBean(val title: String, val imgPath: String, val url: String, val date: String, val view: String,
                        val like: String) : Serializable

data class ChildDetailBean(val title: String, val list: List<String>) : Serializable


data class SeeBigPicBean(val position: Int, val v: ImageView, val urls: ArrayList<String>? = null)
