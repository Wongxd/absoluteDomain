package com.wongxd.absolutedomain.ui.aty

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.filippudak.ProgressPieView.ProgressPieView
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.util.TU
import com.wongxd.absolutedomain.widget.pinchImageview.PinchImageView
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import me.jessyan.progressmanager.ProgressListener
import me.jessyan.progressmanager.ProgressManager
import me.jessyan.progressmanager.body.ProgressInfo
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutionException
import kotlin.properties.Delegates


/**
 * 用于查看大图

 */
class ViewBigImageActivity : BaseSwipeActivity(), ViewPager.OnPageChangeListener {

    companion object {
        // 保存图片
        var tv_save_big_image: TextView? = null
        // 接收传过来的uri地址
        var imageurl: List<String>? = null
        // 用于管理图片的滑动
        var very_image_viewpager: ViewPager by Delegates.notNull()

        // 当前页数
        var page: Int = 0
        //显示当前图片的页数
        var very_image_viewpager_text: TextView by Delegates.notNull()

        var adapter: ViewPagerAdapter by Delegates.notNull()


        val URL = "imageurl"

        fun startActivity(activity: AppCompatActivity, position: Int, urlList: ArrayList<String>?, transitionView: View) {
            val intent = Intent(activity, ViewBigImageActivity::class.java)
            val bundle = Bundle()
            bundle.putStringArrayList(URL, urlList)
            bundle.putInt("page", position)
            intent.putExtras(bundle)

            // 这里指定了共享的视图元素
            val options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, transitionView,
                            activity.resources.getString(R.string.iv_transitions_name))

            ActivityCompat.startActivity(activity, intent, options.toBundle())

        }

        /**
         * 保存图片至相册
         */
        fun saveImageToGallery(context: Context, bmp: Bitmap) {
            // 首先保存图片
            val appDir = File(Environment.getExternalStorageDirectory(), context.packageName)
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            //         其次把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(context.contentResolver,
                        file.absolutePath, fileName, null)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            //         最后通知图库更新
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.absoluteFile)))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_big_image)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getView()
    }


    /**
     * Glide 获得图片缓存路径
     */
    private fun getImagePath(imgUrl: String): String? {
        var path: String? = null
        val future = Glide.with(this@ViewBigImageActivity)
                .load(imgUrl)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        try {
            val cacheFile = future.get()
            path = cacheFile.absolutePath
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } finally {
            return path
        }
    }


    /*
     * 接收控件
     */
    private fun getView() {
        /************************* 接收控件  */
        very_image_viewpager_text = findViewById(R.id.very_image_viewpager_text) as TextView
        tv_save_big_image = findViewById(R.id.tv_save_big_image) as TextView
        very_image_viewpager = findViewById(R.id.very_image_viewpager) as ViewPager

        tv_save_big_image!!.setOnClickListener {
            TU.cT("开始下载图片")
            val bmOptions = BitmapFactory.Options()
            doAsync {
                // 子线程获得图片路径
                val imagePath = getImagePath(imageurl!![page])
                var bitmap: Bitmap? = null
                if (!TextUtils.isEmpty(imagePath))
                    bitmap = BitmapFactory.decodeFile(imagePath, bmOptions)
                // 主线程更新
                uiThread {
                    if (bitmap != null) {
                        saveImageToGallery(this@ViewBigImageActivity, bitmap!!)
                        TU.cT("已保存至" + Environment.getExternalStorageDirectory().absolutePath + "/" + packageName)
                    } else
                        TU.cT("保存失败")

                }
            }

        }
        /************************* 接收传值  */
        val bundle = intent.extras
        page = bundle.getInt("page")
        imageurl = bundle.getStringArrayList("imageurl")
        /**
         * 给viewpager设置适配器
         */
        adapter = ViewPagerAdapter()
        very_image_viewpager.adapter = adapter
        very_image_viewpager.currentItem = page
        very_image_viewpager.addOnPageChangeListener(this)
        very_image_viewpager.isEnabled = false
        // 设定当前的页数和总页数
        very_image_viewpager_text.text = (page + 1).toString() + " / " + imageurl!!.size

    }

    /**
     * 添加进度监听
     * @param position
     * @param url
     */
    fun addProgressListener(position: Int, url: String) {

        ProgressManager.getInstance().addResponseListener(url, object : ProgressListener {
            override fun onProgress(progressInfo: ProgressInfo?) {

                RxBus.getDefault().post(position, progressInfo?.percent)
                if(progressInfo?.isFinish!!) {
                    RxBus.getDefault().post(position, -1)
                    disposeMap[position]?.dispose()
                }
            }

            override fun onError(id: Long, e: Exception?) {
                RxBus.getDefault().post(position, -1)
                disposeMap[position]?.dispose()
            }
        })
    }


    val disposeMap: MutableMap<Int, Disposable> = HashMap()

    /**
     * ViewPager的适配器
     */
    inner class ViewPagerAdapter : PagerAdapter() {
        var inflater: LayoutInflater = layoutInflater
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = inflater.inflate(R.layout.viewpager_very_image, container, false)
            val zoom_image_view = view.findViewById(R.id.zoom_image_view) as PinchImageView

            val spinner = view.findViewById(R.id.loading) as ProgressPieView
            // 保存网络图片的路径
            val adapter_image_Entity = getItem(position) as String

            spinner.visibility = View.VISIBLE
            spinner.isClickable = false


            //进度相关
            addProgressListener(position, adapter_image_Entity)

            val dis = RxBus.getDefault().toObservable(position, Integer::class.java).subscribe(Consumer {
                if (it.toInt() == -1) {
                    spinner.visibility = View.GONE
                } else spinner.progress = it.toInt()
            })
            disposeMap.put(position, dis)

            Glide.with(this@ViewBigImageActivity)
                    .load(adapter_image_Entity)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade(700)
                    .listener(object : RequestListener<String, GlideDrawable> {
                        override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?,
                                                     isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                            spinner.visibility = View.GONE

                            /**这里应该是加载成功后图片的高 */
                            val height = zoom_image_view.height

                            val wHeight = windowManager.defaultDisplay.height
                            if (height > wHeight) {
                                zoom_image_view.scaleType = ImageView.ScaleType.CENTER_CROP
                            } else {
                                zoom_image_view.scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                            return false
                        }

                        override fun onException(e: java.lang.Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                            spinner.visibility = View.GONE
                            return false
                        }


                    })
                    .into(zoom_image_view)

            view.tag = position
            container.addView(view, 0)
            return view
        }

        override fun getCount(): Int {
            if (imageurl == null || imageurl!!.isEmpty()) {
                return 0
            }
            return imageurl!!.size
        }

        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
            disposeMap[position]?.dispose()
        }

        fun getItem(position: Int): Any {
            return imageurl!![position]
        }
    }

    /**
     * 下面是对Viewpager的监听
     */
    override fun onPageScrollStateChanged(arg0: Int) {}

    override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

    override fun onPageSelected(arg0: Int) {
        // 每当页数发生改变时重新设定一遍当前的页数和总页数
        very_image_viewpager_text.text = (arg0 + 1).toString() + " / " + imageurl!!.size
        page = arg0
    }

    override fun onDestroy() {
        RxBus.getDefault().post(RxEventCodeType.IMG_LIST_POSTION_CHANGE, page)
        for (i in disposeMap.values) i.dispose()
        super.onDestroy()
    }


}
