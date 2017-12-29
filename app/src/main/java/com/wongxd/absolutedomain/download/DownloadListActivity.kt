package com.wongxd.absolutedomain.download

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.util.TU
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_download_list.*
import zlc.season.rxdownload3.RxDownload
import zlc.season.rxdownload3.core.*
import zlc.season.rxdownload3.extension.ApkInstallExtension
import zlc.season.rxdownload3.helper.dispose
import zlc.season.rxdownload3.helper.loge
import java.io.File


class DownloadListActivity : BaseSwipeActivity() {
    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_list)


        adapter = Adapter()
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter

        startAll.setOnClickListener {
            RxDownload.startAll().subscribe()
        }

        stopAll.setOnClickListener {
            RxDownload.stopAll().subscribe()
        }

        deleteAll.setOnClickListener {
            RxDownload.deleteAll().subscribe {
                loadData()
            }
        }

        loadData()
    }

    private fun loadData() {
        RxDownload.getAllMission()
                .observeOn(mainThread())
                .subscribe {
                    adapter.addData(it)
                }
    }


    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        val data = mutableListOf<Mission>()

        fun addData(data: List<Mission>) {
            this.data.clear()
            this.data.addAll(data)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent!!.context)

            val v = inflater.inflate(R.layout.view_holder_download_item, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            holder?.setData(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onViewAttachedToWindow(holder: ViewHolder?) {
            super.onViewAttachedToWindow(holder)
            holder?.onAttach()
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder?) {
            super.onViewDetachedFromWindow(holder)
            holder?.onDetach()
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var mission: Mission? = null
        private var disposable: Disposable? = null
        private var currentStatus: Status? = null

        val icon = view.findViewById<ImageView>(R.id.icon)
        val action = view.findViewById<Button>(R.id.action)
        val percent = view.findViewById<TextView>(R.id.percent)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val size = view.findViewById<TextView>(R.id.size)

        init {
            action.setOnClickListener {
                when (currentStatus) {
                    is Normal -> start()
                    is Suspend -> start()
                    is Failed -> start()
                    is Downloading -> stop()
                    is Succeed -> open()
                    is ApkInstallExtension.Installed -> open()
                }
            }
        }

        private fun start() {
            RxDownload.start(mission!!.url).subscribe({}, { println(it) })
        }

        private fun stop() {
            RxDownload.stop(mission!!.url).subscribe()
        }

        private fun install() {
            RxDownload.extension(mission!!.url, ApkInstallExtension::class.java).subscribe()
        }

        private fun open() {
//            RxDownload.extension(mission!!.url, ApkOpenExtension::class.java).subscribe()

//            Logger.e("savePath---${mission!!.savePath}----savaName----${mission!!.saveName}")
//            openAssignFolder(mission?.savePath + "/" + mission?.saveName)

            val filePath=mission?.savePath + "/" + mission?.saveName
            Logger.e(filePath)
            openImg(filePath)
        }


        private fun openImg(filePath: String) {
            val file = File(filePath)
            if (file.exists()) {
                val mime = MimeTypeMap.getSingleton()
                val ext = file.getName().substring(file.getName().lastIndexOf(".") + 1)
                val type = mime.getMimeTypeFromExtension(ext)
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val contentUri = FileProvider.getUriForFile(this@DownloadListActivity,
                            "com.wongxd.absolutedomain.fileProvider",
                            file)
                    intent.setDataAndType(contentUri, type)
                } else {
                    intent.setDataAndType(Uri.fromFile(file), type)
                }
                startActivity(intent)
            } else {
                TU.cT("文件不存在，是否被删除？")
            }
        }

        private fun openAssignFolder(path: String) {
            val file = File(path)
            if (null == file || !file.exists()) {
                return
            }
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //判读断版本是否在7.0以上
                val imgUri = FileProvider.getUriForFile(this@DownloadListActivity,
                        "com.wongxd.absolutedomain.fileProvider",
                        file)
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(imgUri, "image/*")
            } else {
                intent.setDataAndType(Uri.fromFile(file), "image/*")
            }



            try {
//                startActivity(intent)
                startActivity(Intent.createChooser(intent, "选择浏览工具"))
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }

        }

        fun setData(mission: Mission) {
            this.mission = mission

            Glide.with(itemView.context).load(mission.url).into(icon)
        }

        fun onAttach() {
            disposable = RxDownload.create(mission!!.url)
                    .observeOn(mainThread())
                    .subscribe {
                        if (currentStatus is Failed) {
                            loge("Failed", (currentStatus as Failed).throwable)
                        }
                        currentStatus = it
                        setProgress(it)
                        setActionText(it)
                    }
        }

        fun onDetach() {
            dispose(disposable)
        }

        private fun setProgress(it: Status) {
            progressBar.max = it.totalSize.toInt()
            progressBar.progress = it.downloadSize.toInt()

            percent.text = it.percent()
            size.text = it.formatString()
        }

        private fun setActionText(status: Status) {
            val text = when (status) {
                is Normal -> "开始"
                is Suspend -> "已暂停"
                is Waiting -> "等待中"
                is Downloading -> "暂停"
                is Failed -> "失败"
                is Succeed -> "打开"
                is ApkInstallExtension.Installing -> "安装中"
                is ApkInstallExtension.Installed -> "打开"
                else -> ""
            }
            action.text = text
        }
    }
}