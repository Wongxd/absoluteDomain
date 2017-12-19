package com.wongxd.absolutedomain.ui.aty

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.support.v7.widget.GridLayoutManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.SystemUtils
import com.wongxd.absolutedomain.util.TU
import com.wongxd.absolutedomain.util.file.FileUtils
import com.wongxd.absolutedomain.util.file.JDLYFileFilter
import com.wongxd.absolutedomain.widget.SwipeDeleteLayout.SwipeLayout
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TuFavoriteActivity : BaseSwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_tu_favorite)
        StatusBarUtil.immersive(this)
        StatusBarUtil.setPaddingSmart(this, rv_favorite)
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

        tv_import.setOnClickListener { inportFromFile() }

        tv_export.setOnClickListener {
            AlertDialog.Builder(this).setMessage("需要备份收藏到文件中吗？")
                    .setPositiveButton("需要", { dialog, which -> dialog.dismiss();exportToFile() })
                    .setNeutralButton("不需要", { dialog, which -> dialog.dismiss(); })
                    .setCancelable(false)
                    .show()
        }
    }


    /**
     * 查找所有的备份文件
     */
    private fun queryFilesByJava(): MutableList<JDLY> {

        val list: MutableList<JDLY> = ArrayList()

        try {
            val files = JDLYFileFilter.getAllFilePath(FileUtils.getRootDirPath())

            files.sortByDescending { it.lastModified() }

            for (f in files) {
                val path = f.path
                val size = f.length()
                val dot = path.lastIndexOf("/");
                val name = path.substring(dot + 1);
                list.add(JDLY(name, path, FileUtils.getFileSize(size)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            TU.cT("读取存储卡失败！")
        }

        return list
    }

    data class JDLY(val name: String, val path: String, val size: String)

    private fun inportFromFile() {

        val baks: MutableList<JDLY> = ArrayList()
        baks.addAll(queryFilesByJava())

//        for (i in baks) {
//            Logger.e("baks大小---${baks.size}---${i.path}----${i.name}----${i.size}")
//        }

        if (baks.size <= 0) {
            TU.cT("没有找到您的备份文件")
            return
        }

        AlertDialog.Builder(this).setMessage("需要从文件中增量还原吗？")
                .setPositiveButton("需要", { dialog, which -> dialog.dismiss();showPop(baks) })
                .setNeutralButton("不需要", { dialog, which -> dialog.dismiss(); initData() })
                .setCancelable(false)
                .show()

    }


    private fun showPop(baks: MutableList<JDLY>) {
        val pop = PopupWindow(this)
        SystemUtils.backgroundAlpha(this, 0.7f)
        val v = View.inflate(this, R.layout.layout_bak_list, null)
        val lv = v.findViewById<ListView>(R.id.lv)

        val adapter = LvAdapter(baks)
        adapter.setItemClick(object : LvListener {
            override fun onClick(data: JDLY) {
                doRestore(data.path)
                if (pop.isShowing) pop.dismiss()
            }
        })

        lv.adapter = adapter


        pop.contentView = v
        pop.height = rv_favorite.height - 500
        pop.width = rl_top.width - 200

        pop.isOutsideTouchable = true
        pop.isFocusable = true
        //让pop可以点击外面消失掉
        pop.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        pop.setOnDismissListener { SystemUtils.backgroundAlpha(this, 1f) }
        pop.setTouchInterceptor(View.OnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_OUTSIDE) {
                pop.dismiss();
                return@OnTouchListener true
            }
            false;
        });
        pop.showAsDropDown(rl_top, 100, 20)
    }

    interface LvListener {
        fun onClick(data: JDLY)
    }

    inner class LvAdapter() : BaseAdapter() {

        private lateinit var list: MutableList<JDLY>

        constructor(list: MutableList<JDLY>) : this() {
            this.list = list
        }


        private val swipeList = java.util.ArrayList<SwipeLayout>()

        fun closeOtherSwipe() {
            for (s in swipeList)
                s.close()
        }

        private var listener: LvListener? = null

        fun setItemClick(lis: LvListener) {
            this.listener = lis
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val data = list[position]

            var realView = convertView

            if (realView == null) {
                realView = View.inflate(this@TuFavoriteActivity, R.layout.item_rv_favorite_bak, null)
            }


            val tvName: TextView
            val tvPath: TextView
            val tvSize: TextView



            tvName = realView?.findViewById(R.id.tv_file_name)!!
            tvPath = realView.findViewById(R.id.tv_file_paht)!!
            tvSize = realView.findViewById(R.id.tv_file_size)!!


            val rlItem = realView.findViewById<RelativeLayout>(R.id.rl_item)

            rlItem.setOnClickListener {
                if (listener != null) {
                    listener?.onClick(data)
                }
            }

            val tvDelete = realView.findViewById<TextView>(R.id.tv_delete)


            tvDelete?.setOnClickListener {
                val path = data.path
                val f = File(path)
                if (f.exists()) {
                    val b = f.delete()
                    TU.cT(if (b) "删除成功" else "删除失败")
                    if (b) {
                        list.remove(data)
                        this.notifyDataSetChanged()
                    }
                }
            }

            val swipeLayout = realView.findViewById<SwipeLayout>(R.id.swipelayout)


            swipeLayout?.listener = object : SwipeLayout.OnSwipeListener {
                override fun onSwipe(swipeLayout: SwipeLayout?) {

                }

                override fun onColse(swipeLayout: SwipeLayout?) {
                    swipeList.remove(swipeLayout!!)
                }

                override fun onOpen(swipeLayout: SwipeLayout?) {

                    swipeList.add(swipeLayout!!)
                }

                override fun onStartOpen(swipeLayout: SwipeLayout?) {
                    closeOtherSwipe()
                }

            }


            tvName.text = data.name
            tvPath.text = data.path
            tvSize.text = data.size

            return realView
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

    }


    /**
     * 从备份文件中还原
     */
    private fun doRestore(sPaht: String = "") {
        var path = sPaht
        if (sPaht == "") {
            val uri = intent.data
            path = uri.path
        }
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
            } catch (e: Exception) {
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
                adpater?.setNewData(tuList.toMutableList())
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
                    saveFile(sb.toString(), getString(R.string.app_name) + "-收藏备份" + stampToDate(System.currentTimeMillis()))
                    runOnUiThread { if (pb.isShowing) pb.dismiss() }
                } else {
                    runOnUiThread { if (pb.isShowing) pb.dismiss() }
                }
            }
        }).start()
    }

    /*
     * 将时间戳转换为时间
     */
    fun stampToDate(lt: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date(lt)
        return simpleDateFormat.format(date)
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
            filePath = "/mnt" + File.separator + getString(R.string.app_name) + File.separator + fileName + ".jdly"

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
            helper?.itemView?.setOnLongClickListener { re(helper?.layoutPosition); true }
        }

        fun re(position: Int) {

            //删除收藏
            var isDelete = 0
            this.data.let {
                val item = it[position]
                mContext.tuDB.use {
                    isDelete = delete(TuTable.TABLE_NAME, TuTable.ADDRESS + "=?", arrayOf(item.address))
                }
            }
            if (isDelete != 0) {
                this.remove(position)
            }

        }
    }
}

