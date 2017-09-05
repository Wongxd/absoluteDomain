package com.wongxd.absolutedomain.ui.aty

import android.app.AlertDialog
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
import com.wongxd.wthing_kotlin.database.*
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_tu_favorite.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.File
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
        if (intent != null && intent.action == Intent.ACTION_VIEW) {
            AlertDialog.Builder(this).setMessage("需要从文件中增量还原吗？")
                    .setPositiveButton("需要", { dialog, which -> dialog.dismiss();doRestore() })
                    .setNeutralButton("不需要", { dialog, which -> dialog.dismiss(); initData() })
                    .setCancelable(false)
                    .show()

        } else {
            initData()
        }

        tv_export.setOnClickListener { exportToFile() }
    }

    /**
     * 从备份文件中还原
     */
    private fun doRestore() {
        val uri = intent.data
        val path = uri.path
        if (!path.endsWith(".jdly")) {
            toast("不是一个正确的备份文件！")
        } else {
            val pb = ProgressDialog(this)
            pb.setMessage("还原备份中")
            pb.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            pb.show()
            try {
                val file = File(path)
                val info = file.readText()
                val json = JSONObject(info)
                val list = json.optJSONArray("list")
                var i = 0
                val length = list.length()

                while (i < length) {
                    val obj = list.optJSONObject(i)
                    val name = obj.optString("name")
                    val adress = obj.optString("address")
                    val imgPath = obj.optString("imgPath")
                    restoreToDB(name, adress, imgPath)
                    Logger.e(i.toString() + " " + length + " $obj")
                    i++
                }

                toast("增量还原成功！")
            } catch(e: Exception) {
                e.printStackTrace()
                toast("备份文件损坏！")
            } finally {
                pb.dismiss()
            }

            initData()
        }
    }

    /**
     * 从数据库加载东西
     */
    private fun initData() {
        tuDB.use {
            val list = select(TuTable.TABLE_NAME).parseList { (Tu(HashMap(it))) }
            if (list.isNotEmpty()) {
                val tuList = list.sortedByDescending { it._id }
                adpater?.setNewData(tuList)
                rl_empty.visibility = View.GONE
            }
        }
    }

    /**
     * 导出收藏到文件中
     */
    private fun exportToFile() {
        val pb = ProgressDialog(this@TuFavoriteActivity)
        pb.setMessage("导出中")
        pb.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pb.show()
        Thread(Runnable {

            tuDB.use {
                val list = select(TuTable.TABLE_NAME).parseList { (Tu(HashMap(it))) }
                if (list.isNotEmpty()) {

                    val sb = StringBuilder()
                    sb.append("{\"list\":[")
                    val length = list.size - 1

                    for (i in list.indices) {
                        sb.append("{\"name\":\"")
                        sb.append(list[i].name)
                        sb.append("\",")

                        sb.append("\"address\":\"")
                        sb.append(list[i].address)
                        sb.append("\",")

                        sb.append("\"imgPath\":\"")
                        sb.append(list[i].imgPath)
                        if (i == length) {
                            sb.append("\"}")
                        } else {
                            sb.append("\"},")
                        }
                    }

                    sb.append("]}")
                    Logger.e(sb.toString())
                    saveFile(sb.toString(), getString(R.string.app_name) + "-收藏备份")
                    runOnUiThread { if (pb.isShowing) pb.dismiss() }
                } else {
                    runOnUiThread { if (pb.isShowing) pb.dismiss() }
                }
            }
        }).start()
    }


    /**
     * 还原单条数据
     * @param adress
     * @param name
     */
    fun restoreToDB(name: String, adress: String, imgPath: String) {
        tuDB.use {
            transaction {
                val items = select(TuTable.TABLE_NAME).whereSimple(TuTable.ADDRESS + "=?", adress)
                        .parseList({ Tu(HashMap(it)) })
                if (items.isEmpty()) {  //如果是空的
                    val tu = Tu()
                    tu.address = adress
                    tu.name = name
                    tu.imgPath = imgPath
                    insert(TuTable.TABLE_NAME, *tu.map.toVarargArray())
                }
                Logger.e("还原单条数据  $name  $adress  $imgPath ")
            }
        }
    }

    /**
     * @param content
     *
     */
    fun saveFile(content: String, fileName: String) {
        var filePath: String? = null
        val hasSDCard = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        if (hasSDCard) {
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator + getString(R.string.app_name) + File.separator + fileName + ".jdly"
        } else
        // 系统下载缓存根目录的hello.text
            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + getString(R.string.app_name) + File.separator + fileName + ".jdly"

        try {
            val file = File(filePath)
            if (!file.exists()) {
                val dir = File(file.parent)
                dir.mkdirs()
                file.createNewFile()
            } else {
                file.delete()
            }
            file.writeText(content)
            runOnUiThread { toast("导出成功！位于《 " + getString(R.string.app_name) + " 》文件夹下") }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { toast("导出失败！") }
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

            //删除收藏
            var isDelete = 0
            adpater?.data?.let {
                val item = it[position]
                tuDB.use {
                    isDelete = delete(TuTable.TABLE_NAME, TuTable.ADDRESS + "=?", arrayOf(item.address))
                }
            }
            if (isDelete != 0) {
                 initData()
//                adpater?.notifyDataSetChanged()
//                var newPos = 0
//                if (position!=0){
//                    newPos = position - 1
//
//                }
//                rv_favorite.smoothScrollToPosition(newPos)
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

        fun re(pos: Int) {
            data.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }
}

