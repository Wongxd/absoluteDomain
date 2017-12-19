package com.wongxd.absolutedomain.ui.aty

import android.app.AlertDialog
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
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.listener.DeleteListener
import cn.bmob.v3.listener.UpdateListener
import cn.bmob.v3.listener.UploadFileListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.App
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.bean.UserBean
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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.File
import java.net.URL
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
            AlertDialog.Builder(this).setMessage("需要从 文件 中增量还原吗？").setTitle("警告")
                    .setPositiveButton("需要", { dialog, which -> dialog.dismiss();doRestoreFromFile() })
                    .setNeutralButton("不需要", { dialog, which -> dialog.dismiss(); initData() })
                    .setCancelable(false)
                    .show()

        } else {
            initData()
        }

        tv_import.setOnClickListener {
            AlertDialog.Builder(this).setTitle("警告").setMessage("需要从 文件 或 云 中增量还原吗？")
                    .setPositiveButton("从文件", { dialog, which -> dialog.dismiss();inportFromFile() })
                    .setNegativeButton("从云中", { dialog, which -> dialog.dismiss();doRestoreFromBmob() })
                    .setNeutralButton("不需要", { dialog, which -> dialog.dismiss(); })
                    .setCancelable(false)
                    .show()
        }

        tv_export.setOnClickListener {
            AlertDialog.Builder(this).setMessage("需要备份收藏到 文件 或 云  中吗？").setTitle("警告")
                    .setPositiveButton("到文件", { dialog, which -> dialog.dismiss();exportToFile() })
                    .setNegativeButton("到云中", { dialog, which -> dialog.dismiss();exportToBmob() })
                    .setNeutralButton("不需要", { dialog, which -> dialog.dismiss(); })
                    .setCancelable(false)
                    .show()
        }
    }

    /**
     * 从云中还原
     */
    private fun doRestoreFromBmob() {

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "从云端同步"
        pDialog.setCancelable(false)
        pDialog.show()

        val current = BmobUser.getCurrentUser(this@TuFavoriteActivity, UserBean::class.java)
        if (current == null) {
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
            pDialog.contentText = "请先登录"
            pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
            return
        }

        val bmobF = current.favorite
        if (bmobF == null) {
            pDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE)
            pDialog.contentText = "云端没有备份"
            pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
            return
        }
        Logger.e("从云端还原备份文件--" + bmobF.getFileUrl(this))

        doAsync {
            val info = URL(bmobF.getFileUrl(this@TuFavoriteActivity)).readText() ?: " "

            val json = JSONObject(info)
            val list = json.optJSONArray("list")
            var i = 0
            val length = list.length()

            if (list.length() == 0) {
                uiThread {
                    pDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE)
                    pDialog.contentText = "云中没有备份"
                    pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                }
                return@doAsync
            }

            while (i < length) {
                val obj = list.optJSONObject(i)
                val name = obj.optString("name")
                val adress = obj.optString("address")
                val imgPath = obj.optString("imgPath")
                restoreToDB(name, adress, imgPath)
                i++
            }

            uiThread {
                pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                pDialog.contentText = "从云端同步完成"
                pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                initData()
            }

        }

    }

    /**
     *把收藏信息同步到云
     */
    private fun exportToBmob() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "同步到云端"
        pDialog.setCancelable(false)
        pDialog.show()

        val current = BmobUser.getCurrentUser(this@TuFavoriteActivity, UserBean::class.java)
        if (current == null) {
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
            pDialog.contentText = "请先登录"
            pDialog.setCancelClickListener {
                pDialog.dismissWithAnimation()
            }
            return
        }

        doAsync {
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

                    val file = File(FileUtils.getRootDirPath() + current.username + "--cloud-temp.ttt")
                    if (!file.exists()) {
                        val dir = File(file.parent)
                        dir.mkdirs()
                        file.createNewFile()
                    } else {
                        file.delete()
                    }
                    file.writeText(sb.toString())

                    val bmobFile = BmobFile(file)
                    //上传新的备份文件
                    bmobFile.uploadblock(this@TuFavoriteActivity, object : UploadFileListener() {
                        override fun onSuccess() {

                            //删除旧的备份文件
                            val oldFile = current.favorite
                            oldFile?.delete(this@TuFavoriteActivity, object : DeleteListener() {
                                override fun onSuccess() {
                                    Logger.d("图集备份到云端", "清除历史备份成功")
                                }

                                override fun onFailure(p0: Int, p1: String?) {
                                    TU.cT("清除历史备份失败---$p1")
                                }
                            })

                            //备份文件与用户关联
                            val user = UserBean()
                            user.favorite = bmobFile
                            user.update(this@TuFavoriteActivity, current.objectId, object : UpdateListener() {
                                override fun onSuccess() {
                                    uiThread {
                                        App.user = BmobUser.getCurrentUser(this@TuFavoriteActivity, UserBean::class.java)
                                        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                        pDialog.contentText = "成功备份到云端"
                                        pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                                    }
                                }

                                override fun onFailure(p0: Int, p1: String?) {
                                    uiThread {
                                        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                        pDialog.contentText = p1
                                        pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                                    }
                                }
                            })
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            uiThread {
                                pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                pDialog.contentText = p1
                                pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                            }
                        }
                    })


                } else {
                    uiThread {
                        pDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE)
                        pDialog.contentText = "没有收藏的图集"
                        pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                    }
                }
            }
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

        showPop(baks)
    }


    private fun showPop(baks: MutableList<JDLY>) {
        val pop = PopupWindow(this)
        SystemUtils.backgroundAlpha(this, 0.7f)
        val v = View.inflate(this, R.layout.layout_bak_list, null)
        val lv = v.findViewById<ListView>(R.id.lv)

        val adapter = LvAdapter(baks)
        adapter.setItemClick(object : LvListener {
            override fun onClick(data: JDLY) {
                doRestoreFromFile(data.path)
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
    private fun doRestoreFromFile(sPaht: String = "") {
        var path = sPaht
        if (sPaht == "") {
            val uri = intent.data
            path = uri.path
        }
        if (path.isBlank()) return
        if (!path.endsWith(".jdly")) {
            toast("不是一个正确的备份文件！")
        } else {
            val pDialog = SweetAlertDialog(this@TuFavoriteActivity, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "从文件中还原"
            pDialog.setCancelable(false)
            pDialog.show()
            doAsync {
                try {
                    val file = File(path)
                    val info = file.readText()
                    val json = JSONObject(info)
                    val list = json.optJSONArray("list")
                    var i = 0
                    val length = list.length()

                    if (list.length() == 0) {
                        uiThread {
                            pDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE)
                            pDialog.contentText = "文件中没有备份"
                            pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                        }
                        return@doAsync
                    }

                    while (i < length) {
                        val obj = list.optJSONObject(i)
                        val name = obj.optString("name")
                        val adress = obj.optString("address")
                        val imgPath = obj.optString("imgPath")
                        restoreToDB(name, adress, imgPath)
                        i++
                    }

                    uiThread {
                        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                        pDialog.contentText = "增量还原成功"
                        pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                        initData()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    uiThread {
                        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                        pDialog.contentText = "备份文件损坏"
                        pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                    }
                }

            }
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
            }
        }
    }

    /**
     * 导出收藏到文件中
     */
    private fun exportToFile() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "导出收藏到本地文件"
        pDialog.setCancelable(false)
        pDialog.show()

        doAsync {
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
                    Logger.d(sb.toString())
                    val result = saveFile(sb.toString(), getString(R.string.app_name) + "-收藏备份" + stampToDate(System.currentTimeMillis()))
                    uiThread {
                        pDialog.changeAlertType(if (result) SweetAlertDialog.SUCCESS_TYPE else SweetAlertDialog.ERROR_TYPE)
                        pDialog.contentText = if (result) "导出成功！位于《 绝对领域 》文件夹下" else "导出失败"
                        pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                    }
                } else {
                    uiThread {
                        pDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE)
                        pDialog.contentText = "没有收藏的图集"
                        pDialog.setCancelClickListener { pDialog.dismissWithAnimation() }
                    }
                }
            }
        }
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
                Logger.d("还原单条数据  $name  $adress  $imgPath ")
            }
        }
    }

    /**
     * @param content
     *
     */
    fun saveFile(content: String, fileName: String): Boolean {
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
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    private lateinit var adpater: TuAdapter
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
        val layoutManager = GridLayoutManager(applicationContext, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (adpater.data.size == 0) return 2
                return 1
            }

        }
        rv_favorite.layoutManager = layoutManager
        adpater.setEmptyView(R.layout.item_rv_empty, rv_favorite)


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

