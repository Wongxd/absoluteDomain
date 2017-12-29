package com.wongxd.absolutedomain


import android.Manifest
import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.transition.Explode
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobUser
import com.jude.swipbackhelper.SwipeBackHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.bean.TypeBean
import com.wongxd.absolutedomain.bean.UserBean
import com.wongxd.absolutedomain.download.DownloadListActivity
import com.wongxd.absolutedomain.fgt.BaseTypeFragment
import com.wongxd.absolutedomain.fgt.jdlingyu.JdlingyuFgt
import com.wongxd.absolutedomain.fgt.keke123.KeKe123Fgt
import com.wongxd.absolutedomain.fgt.meisiguan.MeisiGuanFgt
import com.wongxd.absolutedomain.fgt.mmonly.MMonlyFgt
import com.wongxd.absolutedomain.fgt.mntu92.Mntu92Fgt
import com.wongxd.absolutedomain.fgt.nvshens.NvshensFgt
import com.wongxd.absolutedomain.fgt.t192tt.t192ttFgt
import com.wongxd.absolutedomain.login.LoginActivity
import com.wongxd.absolutedomain.ui.aty.ThemeActivity
import com.wongxd.absolutedomain.ui.aty.TuFavoriteActivity
import com.wongxd.absolutedomain.user.UserInfoActivity
import com.wongxd.absolutedomain.util.AlipayUtil
import com.wongxd.absolutedomain.util.SPUtils
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import com.wongxd.absolutedomain.util.apk.UpdateUtil
import com.wongxd.absolutedomain.util.cache.DataCleanManager
import com.wongxd.absolutedomain.util.cache.GlideCatchUtil
import com.wongxd.partymanage.base.kotin.extension.loadHeader
import kotlinx.android.synthetic.main.aty_main.*
import java.util.*


class AtyMainActivity : BaseSwipeActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_download_list -> startActivity(Intent(this, DownloadListActivity::class.java))
            R.id.menu_theme -> startActivity(Intent(this, ThemeActivity::class.java))
            R.id.menu_cache -> cacheThing()
            R.id.menu_about -> showAbout()
            R.id.menu_tu_favorite -> startActivity(Intent(this, TuFavoriteActivity::class.java))
            R.id.menu_donate -> Donate()
            R.id.menu_alipay_red -> getRedpacket()

        //右边
            R.id.menu_jdlingyu -> switchSite(1)
            R.id.menu_keke123 -> switchSite(2)
            R.id.menu_192tt -> switchSite(3)
//            R.id.menu_mmonly -> switchSite(4)
            R.id.menu_nvshens -> switchSite(5)
            R.id.menu_meisiguan -> switchSite(6)
//            R.id.menu_mntu92 -> switchSite(7)
            R.id.menu_update -> UpdateUtil.goToAppMarket(this)

        }


        drawerlayout.postDelayed({
            if (drawerlayout.isDrawerOpen(nav_aty_main))
                drawerlayout.closeDrawer(nav_aty_main)
            if (drawerlayout.isDrawerOpen(nav_aty_main_right)) {
                drawerlayout.closeDrawer(nav_aty_main_right)
            }
        }, 500)

        return true
    }


    private lateinit var currentFgt: BaseTypeFragment

    private fun switchSite(flag: Int) {

        currentFgt.onDestroyView()
        currentFgt.onDetach()

        when (flag) {
            1 -> {
                tv_title_aty_main.text = "jdlingyu"
                currentTypeList = JdlingyuFgt.typeList
                currentFgt = JdlingyuFgt()
            }
            2 -> {
                tv_title_aty_main.text = "keke123"
                currentTypeList = KeKe123Fgt.typeList
                currentFgt = KeKe123Fgt()
            }
            3 -> {
                tv_title_aty_main.text = "192tt"
                currentTypeList = t192ttFgt.typeList
                currentFgt = t192ttFgt()
            }
            4 -> {

                tv_title_aty_main.text = "mmonly"
                currentTypeList = MMonlyFgt.typeList
                currentFgt = MMonlyFgt()
            }
            5 -> {
                tv_title_aty_main.text = "nvshens"
                currentTypeList = NvshensFgt.typeList
                currentFgt = NvshensFgt()
            }

            6 -> {
                tv_title_aty_main.text = "meisiguan"
                currentTypeList = MeisiGuanFgt.typeList
                currentFgt = MeisiGuanFgt()
            }

            7 -> {
                tv_title_aty_main.text = "mntu92"
                currentTypeList = Mntu92Fgt.typeList
                currentFgt = Mntu92Fgt()
            }

        }

        initTablayout()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container_aty_main, currentFgt)
                .commitNow()
    }

    private fun eMailMe() {
        val data = Intent(Intent.ACTION_SENDTO)
        data.data = Uri.parse("mailto:974501076@qq.com")
        data.putExtra(Intent.EXTRA_SUBJECT, "\"绝对领域\" 反馈")
        data.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(data)
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage("数据来源于网络，仅供学习交流使用。切勿 违法及商用。对滥用本软件造成的一切后果，请自行承担。\n如有侵权，请联系该网站管理员。")
                .setNeutralButton("联系我") { dialog, which -> eMailMe() }
                .create()
                .show()
    }

    fun initPermission() {
        val permissions = RxPermissions(this)
        permissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_WIFI_STATE)
                .subscribe { // will emit 2 Permission objects
                    permission ->
                    if (permission.granted) {
                        // `permission.name` is granted !


                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // Denied permission without ask never again
                        val perName = if (permission.name == Manifest.permission.READ_EXTERNAL_STORAGE) "读取存储卡" else "访问wifi状态"
                        TU.cT(perName + " 权限被禁止，无法进行操作")
                    } else {
                        // Denied permission with ask never again
                        // Need to go to the settings
                        val perName = if (permission.name == Manifest.permission.READ_EXTERNAL_STORAGE) "读取存储卡" else "访问wifi状态"
                        val dialog = AlertDialog.Builder(this)
                                .setMessage(perName + "\n权限被禁止，请到 设置-权限 中给予")
                                .setPositiveButton("确定", { dialog1, which ->
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                })
                                .create()
                        dialog.show()
                    }
                }
    }

    /**
     * 缓存
     */
    private fun cacheThing() {
        val imgCache = GlideCatchUtil.getInstance().cacheSize
        val totalCache = DataCleanManager.getTotalCacheSize(applicationContext)
        AlertDialog.Builder(this)
                .setTitle("缓存信息")
                .setMessage("图片缓存: $imgCache \n全部缓存: $totalCache")
                .setNeutralButton("清除全部缓存", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        DataCleanManager.clearAllCache(applicationContext)
                        dialog?.dismiss()
                    }

                })
                .setNegativeButton("清除图片缓存", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        GlideCatchUtil.getInstance().clearCacheDiskSelf()
                        dialog?.dismiss()
                    }

                })
                .create()
                .show()
    }


    override fun onDestroy() {
        currentFgt.onDestroy()
        RxBus.getDefault().unRegister(this)
        super.onDestroy()
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val explode = Explode()
            explode.duration = 500
            window.exitTransition = explode
            window.enterTransition = explode
        }

        //状态栏透明和间距处理
        StatusBarUtil.immersive(this)
        StatusBarUtil.setMargin(this, toolbar_aty_main)


        RxBus.getDefault().register(this)


        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(false)
                .setSwipeRelateEnable(true)

        tv_menu_aty_main.setOnClickListener {
            if (drawerlayout.isDrawerOpen(nav_aty_main))
                drawerlayout.closeDrawer(nav_aty_main)
            else
                drawerlayout.openDrawer(nav_aty_main)
        }


        tv_switch_aty_main.setOnClickListener {
            if (drawerlayout.isDrawerOpen(nav_aty_main_right))
                drawerlayout.closeDrawer(nav_aty_main_right)
            else
                drawerlayout.openDrawer(nav_aty_main_right)

        }


        initUserHeader()
        nav_aty_main.setNavigationItemSelectedListener(this)
        nav_aty_main_right.setNavigationItemSelectedListener(this)

        initPermission()

        tv_title_aty_main.text = "nvshens"
        currentTypeList = NvshensFgt.typeList
        initTablayout()
        currentFgt = NvshensFgt()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container_aty_main, currentFgt)
                .commit()

        Bmob.initialize(this, App.BMOB_ID)
        App.user = BmobUser.getCurrentUser(this, UserBean::class.java)

        RxBus.getDefault().toObservable(RxEventCodeType.LOGOUT, String::class.java)
                .subscribe {
                    showUserInfo()
                }

        val c = Calendar.getInstance()//
//                    val mYear = c.get(Calendar.YEAR) // 获取当前年份
//                    val mMonth = c.get(Calendar.MONTH) + 1// 获取当前月份
        val mDay = c.get(Calendar.DAY_OF_MONTH)// 获取当日期
//                    val mWay = c.get(Calendar.DAY_OF_WEEK)// 获取当前日期的星期
//                    val mHour = c.get(Calendar.HOUR_OF_DAY)//时
//                    val mMinute = c.get(Calendar.MINUTE)//分
        val spDay = SPUtils.get(key = "alipayRedCount", defaultObject = 0)

        if (spDay != mDay)
            getRedpacket()
    }


    fun openUrl(url: String) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    /**
     * 捐赠
     */
    fun Donate() {
        QMUIDialog.MessageDialogBuilder(this)
                .setTitle("么么哒")
                .setMessage("捐赠开发者？（开发者升级成商家账户了，可以用支付宝红包抵扣了）\n\n"
                        +"本应用已在规划除图片浏览的其它功能，而且对ui也是要重新设计。捐赠是个很好的激励嘛，嘿嘿。")
                .addAction("捐赠") { dialog, index ->
                    AlipayUtil.startAlipayClient(this, "FKX07373TRZS7EQ7SUVI9A")
//                    openUrl("https://ds.alipay.com/?from=mobilecodec&scheme=alipays%3A%2F%2Fplatformapi%2Fstartapp%3FsaId%3D10000007%26clientVersion%3D3.7.0.0718%26qrcode%3Dhttps%253A%252F%252Fqr.alipay.com%252FFKX07373TRZS7EQ7SUVI9A%253F_s%253Dweb-other")
                    dialog.dismiss()
                }
                .addAction("不捐赠") { dialog, index -> dialog.dismiss() }
                .show()
    }

    fun getRedpacket() {
        QMUIDialog.MessageDialogBuilder(this)
                .setTitle("么么哒")
                .setMessage("领取支付宝红包，帮助开发者领取赏金吗？\n如果没有地方使用红包，可以在应用内打赏给开发者（开发者升级成商家账户了，可以用红包抵扣了）。\n\n"
                +"本应用已在规划除图片浏览的其它功能，而且对ui也是要重新设计。捐赠是个很好的激励嘛，嘿嘿。")
                .addAction("领取") { dialog, index ->
                    //                    openUrl("https://qr.alipay.com/c1x03491e5pr1lnuoid3e22")
                    AlipayUtil.startAlipayClient(this, "c1x03491e5pr1lnuoid3e22")

                    val c = Calendar.getInstance()//
//                    val mYear = c.get(Calendar.YEAR) // 获取当前年份
//                    val mMonth = c.get(Calendar.MONTH) + 1// 获取当前月份
                    val mDay = c.get(Calendar.DAY_OF_MONTH)// 获取当日期
//                    val mWay = c.get(Calendar.DAY_OF_WEEK)// 获取当前日期的星期
//                    val mHour = c.get(Calendar.HOUR_OF_DAY)//时
//                    val mMinute = c.get(Calendar.MINUTE)//分
                    SPUtils.put(key = "alipayRedCount", `object` = mDay)
                    showDonateTips()
                    dialog.dismiss()
                }
                .addAction("不需要") { dialog, index -> dialog.dismiss() }
                .show()
    }

    /**
     * 提醒用户可以把红包 打赏给我
     */
    private fun showDonateTips() {
        QMUIDialog.MessageDialogBuilder(this)
                .setTitle("么么哒")
                .setMessage("如果没有地方使用支付宝红包，可以在应用内打赏给开发者（开发者升级成商家账户了，可以用红包抵扣了）。\n\n" +
                        "本应用已在规划除图片浏览的其它功能，而且对ui也是要重新设计。捐赠是个很好的激励嘛，嘿嘿。")
                .addAction("将红包打赏给开发者") { dialog, index ->
                    AlipayUtil.startAlipayClient(this, "FKX07373TRZS7EQ7SUVI9A");
                    dialog.dismiss()
                }
                .addAction("不用了") { dialog, index -> dialog.dismiss() }
                .show()
    }

    lateinit var currentTypeList: ArrayList<TypeBean>

    fun initTablayout() {
        tablayout_main.removeAllTabs()
        for (t in currentTypeList) {
            val tab = tablayout_main.newTab()
            tab.text = t.title
            tab.tag = t.url

            tablayout_main.addTab(tab)
        }

        tablayout_main.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val url = tab?.tag as String
                RxBus.getDefault().post(RxEventCodeType.SITE_SWITCH, url)
//                Logger.e("发送具体分类的url地址--$url")
            }

        })
    }


    private lateinit var ivHeader: ImageView
    private lateinit var tvUserName: TextView
    private fun initUserHeader() {
        val header = nav_aty_main.getHeaderView(0)
        ivHeader = header.findViewById(R.id.iv_user_header)
        tvUserName = header.findViewById(R.id.tv_user_name)

        header.setOnClickListener {
            if (App.user == null)
                startActivity(Intent(this, LoginActivity::class.java))
            else
                startActivity(Intent(this, UserInfoActivity::class.java))
        }
    }


    override fun onResume() {
        super.onResume()
        showUserInfo()
    }

    private fun showUserInfo() {

        tvUserName.text = "未登录用户"
        ivHeader.loadHeader(R.drawable.not_login_img)

        App.user?.let {
            tvUserName.text = it.username
            ivHeader.loadHeader(R.drawable.login_suc)
        }
    }


}
