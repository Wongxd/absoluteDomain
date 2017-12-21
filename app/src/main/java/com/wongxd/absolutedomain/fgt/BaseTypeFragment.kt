package com.wongxd.absolutedomain.fgt

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.adapter.RvHomeAdapter
import com.wongxd.absolutedomain.base.BaseLazyFragment
import com.wongxd.absolutedomain.bean.HomeListBean
import com.wongxd.absolutedomain.ui.aty.SeePicActivity
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import com.wongxd.wthing_kotlin.database.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fgt_type.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction

/**
 * Created by wongxd on 2017/11/18.
 *
 */
abstract class BaseTypeFragment : BaseLazyFragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fgt_type, container, false)

        return v
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecycle()
        initRefreshLayout()

        rv_fgt_type.setPadding(0, rv_fgt_type.paddingTop + StatusBarUtil.getStatusBarHeight(activity), 0, 0)

        if (tempObservalble != null)
            loadData(tempObservalble!!)
    }

    /**
     * recycleView and smartRefreshLayout
     */
    private fun initRecycle() {

        adpater = RvHomeAdapter {
            val intent = Intent(activity, SeePicActivity::class.java)
            intent.putExtra("url", it.url)
            intent.putExtra("imgPath", it.imgPath)
            intent.putExtra("title", it.title)
            startActivity(intent)
        }
        adpater.setEnableLoadMore(true)
        adpater.setOnItemLongClickListener { adapter1, view1, position ->
            //收藏
            adpater.data.let {
                val bean = it[position]
                activity.tuDB.use {
                    transaction {
                        val items = select(TuTable.TABLE_NAME).whereSimple(TuTable.ADDRESS + "=?", bean.url)
                                .parseList({ Tu(HashMap(it)) })

                        if (items.isEmpty()) {  //如果是空的
                            val tu = Tu()
                            tu.address = bean.url
                            tu.name = bean.title
                            tu.imgPath = bean.imgPath
                            insert(TuTable.TABLE_NAME, *tu.map.toVarargArray())
                        } else {
                            delete(TuTable.TABLE_NAME, TuTable.ADDRESS + "=?", arrayOf(bean.url))
                        }
                        adpater.notifyItemChanged(position, "changeFavorite")
                    }
                }
            }

            return@setOnItemLongClickListener true
        }


        rv_fgt_type.adapter = adpater
        rv_fgt_type.layoutManager = GridLayoutManager(context, 2)
        adpater.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)

    }


    protected fun syncFavorite(p: String) {
        adpater.notifyDataSetChanged()
    }


    private fun initRefreshLayout() {

        srl_fgt_type.setOnRefreshListener {
            currentPage = 1
            doSomethingsWithUrl(currentUrl)
        }

        srl_fgt_type.setOnLoadmoreListener { doSomethingsWithUrl(currentUrl) }
    }


    private var tempObservalble: Observable<List<HomeListBean>>? = null

    protected fun getList(observalble: Observable<List<HomeListBean>>, page: Int = currentPage) {
        if (!isPrepared || null == srl_fgt_type) {
            tempObservalble = observalble
            return //todo不知为何 有时  它为空
        }
        loadData(observalble, page)
    }

    protected fun loadData(observalble: Observable<List<HomeListBean>>, page: Int = currentPage) {

        if (tempObservalble != null) tempObservalble = null

        observalble.observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Consumer<List<HomeListBean>> {
                    @Throws(Exception::class)
                    override fun accept(@NonNull t: List<HomeListBean>) {
                        if (t.isNotEmpty()) {
                            currentPage++
                        } else if (page != 1) {
                            srl_fgt_type.finishLoadmore()
                            adpater.loadMoreEnd()
                        }
                        rl_empty.visibility = View.GONE
                        if (page == 1) {
                            srl_fgt_type.finishRefresh()
                            adpater.setNewData(t)
                        } else {
                            srl_fgt_type.finishLoadmore()
                            adpater.addData(t)
                        }

                        if (adpater.data.size == 0) {
                            rl_empty.visibility = View.VISIBLE
                        }

                    }
                }, Consumer<Throwable>
                {
                    TU.cT(it.message.toString() + " ")
                    if (page == 1) srl_fgt_type.finishRefresh()
                    else srl_fgt_type.finishLoadmore()
                    if (adpater.data.size == 0) {
                        rl_empty.visibility = View.VISIBLE
                    }
                })
    }

    protected var currentPage = 1

    protected lateinit var adpater: RvHomeAdapter


    protected var currentUrl = "https://wongxd.github.io"


    /**
     * 获取对应页面的真实的页面地址
     */
    protected fun getRealPageUrl(originUrl: String, page: Int): String {
        var url = originUrl
        //www.keke123.cc/gaoqing/list_5_2.html
        //页面判断
        var suffix = "page/$page/"
        if (page == 1) {
            suffix = ""
        } else if (url.contains("192tt.com")) {

            suffix = "index_$page.html"

        } else if (url.contains("mmonly.cc")) {

            url = originUrl.substring(0, originUrl.lastIndexOf("_"))
            suffix = "_$page.html"

        } else if (url.contains("keke123")) {

            url = originUrl.substring(0, originUrl.lastIndexOf("_"))
            suffix = "_$page.html"

        } else if (url.contains("nvshens.com")) {
            suffix = "/$page.html"
        }

        return url + suffix
    }


//    ####################################以上为固定代码，以下为动态##############################################

    abstract fun doSomethingsWithUrl(url: String, page: Int = currentPage)

    open fun siteSwitch(url: String) {
        currentUrl = url
        srl_fgt_type.autoRefresh()
    }

    abstract fun doSyncFavorite(p: String)


}