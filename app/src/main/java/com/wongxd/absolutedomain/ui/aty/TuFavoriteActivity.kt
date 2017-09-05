package com.wongxd.absolutedomain.ui.aty

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.orhanobut.logger.Logger
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
import java.io.File
import java.io.FileOutputStream
import java.util.*


class TuFavoriteActivity : BaseSwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_tu_favorite)
        StatusBarUtil.immersive(this)
        StatusBarUtil.setPaddingSmart(this, rv_favorite)
        StatusBarUtil.setPaddingSmart(this, realtime_blur)
        StatusBarUtil.setPaddingSmart(this, rl_top)
        initRecycle()

        tuDB.use {
            val list = select(TuTable.TABLE_NAME).parseList { (Tu(HashMap(it))) }
            if (list.isNotEmpty()) {
                val tuList = list.sortedByDescending { it._id }
                adpater?.setNewData(tuList)
                rl_empty.visibility = View.GONE
            }
        }

//
//        tv_import.setOnClickListener {
//            // Initialize Builder
//            val chooser = StorageChooser.Builder()
//                    .withActivity(this@TuFavoriteActivity)
//                    .withFragmentManager(fragmentManager)
//                    .withMemoryBar(true)
//                    .allowCustomPath(true)
//                    .setType(StorageChooser.FILE_PICKER)
//                    .build()
//
//            // Show dialog whenever you want by
//            chooser.show()
//
//            // get path that the user has chosen
//            chooser.setOnSelectListener {  StorageChooser.OnSelectListener { path ->
//                run {
//                    Log.e("SELECTED_PATH", path)
//                    if (!path.endsWith(".txt")) {
//                        toast("不是一个正确的备份文件！")
//                        return@OnSelectListener
//                    }
//                    val file = File(path)
//                    val s = file.readText()
//                    val json = JSONObject(s)
//                  Logger.e(json.toString())
//
//
//                }
//            }
//            } }
//
//
        tv_export.setOnClickListener { v ->
            run {
                val pb = ProgressDialog(this@TuFavoriteActivity)
                pb.setMessage("导出中")
                pb.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                pb.show()
                Thread(Runnable {

                    tuDB.use {
                        val list = select(TuTable.TABLE_NAME).parseList { (Tu(HashMap(it))) }
                        if (list.isNotEmpty()) {

//                            val json = Gson().toJson(list)
//                            Logger.e(json)
                            val sb = StringBuilder()
                            sb.append("{\"list\":[")

                            for (item in list){
                                sb.append("{\"name\":\"")
                                sb.append(item.name)
                                sb.append("\",")

                                sb.append("\"address\":\"")
                                sb.append(item.address)
                                sb.append("\",")

                                sb.append("{\"imgPath\":\"")
                                sb.append(item.imgPath)
                                sb.append("\"},")
                            }
                            sb.append("]}")
                            Logger.e(sb.toString())
                            saveFile(sb.toString(), "收藏备份-")
                          runOnUiThread {  if (pb.isShowing) pb.dismiss() }
                        } else {
                            runOnUiThread {  if (pb.isShowing) pb.dismiss() }
                        }
                    }
                }).start()
            }
        }
    }

    /**
     * @param content
     */
    fun saveFile(content: String, fileName: String) {
        var filePath: String? = null
        val hasSDCard = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        if (hasSDCard) {
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator + "绝对领域W" + File.separator + fileName + ".txt"
        } else
        // 系统下载缓存根目录的hello.text
            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + "绝对领域W" + File.separator + fileName + ".txt"

        try {
            val file = File(filePath)
            if (!file.exists()) {
                val dir = File(file.parent)
                dir.mkdirs()
                file.createNewFile()
            } else {
                file.delete()
            }
            val outStream = FileOutputStream(file)
            outStream.write(content.toByteArray())
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
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

