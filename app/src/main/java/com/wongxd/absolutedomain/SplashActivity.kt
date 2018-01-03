package com.wongxd.absolutedomain

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.View
import com.luomi.lm.ad.ADType
import com.luomi.lm.ad.DRAgent
import com.luomi.lm.ad.IAdSuccessBack
import com.luomi.lm.ad.LogUtil
import com.orhanobut.logger.Logger
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wongxd.absolutedomain.base.BaseActivity
import com.wongxd.absolutedomain.util.SystemUtils
import com.wongxd.absolutedomain.util.TU
import kotlinx.android.synthetic.main.aty_splash.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

/**
 * Created by wongxd on 2018/1/3.
 *
 */
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_splash)
        initPermission()
    }

    fun initPermission() {
        val permissions = RxPermissions(this)
        permissions.requestEach(Manifest.permission.READ_PHONE_STATE)
                .subscribe { // will emit 2 Permission objects
                    permission ->
                    if (permission.granted) {
                        // `permission.name` is granted !
//                        getAds()
                        showAds()

                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // Denied permission without ask never again
                        val perName = "访问手机状态"
                        TU.cT(perName + " 权限被禁止，无法进行操作")
                    } else {
                        // Denied permission with ask never again
                        // Need to go to the settings
                        val perName = "访问手机状态"
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


    fun getAds() {
        val z = "560"
        val appkey = "65a3f31939037d2f2329fcf80a1069ca"
        val deviceId = SystemUtils.getDeviceUniqID(this)
        val sw = QMUIDisplayHelper.getScreenWidth(this)
        val sh = QMUIDisplayHelper.getScreenHeight(this)
        val osver = android.os.Build.VERSION.SDK_INT.toString()


        doAsync {
            val url = "http://sdk.cferw.com/api.php?z=$z&appkey=$appkey&deviceId=$deviceId&sw=$sw&sh=$sh&osver=$osver"
            val s = URL(url).readText()
            uiThread {
                Logger.e(url)
                Logger.e(s)
            }
        }


    }


    fun showAds() {
        LogUtil.setENABLE_LOGCAT(false)
        /**
         * this  上下文
         * adtype 广告类型（详情请看附录表）
         * true  针对开屏是否显示倒计时展示 针对banner是是否显示关闭按钮
         * IAdSuccessBack 广告展示回调接口
         */
        DRAgent.getInstance().getOpenView(applicationContext, ADType.FULL_SCREEN, true, object : IAdSuccessBack {

            override fun onError(result: String) {
                println(">>>>>>广告展示失败:" + result)
                startActivity(Intent(this@SplashActivity, AtyMainActivity::class.java))
                finish()
            }

            override fun onClick(result: String) {
                println(">>>>>广告被点击:" + result)
            }

            override fun OnSuccess(result: String) {
                println(">>>广告展示成功:" + result)
                if (result == "7") {
                    startActivity(Intent(this@SplashActivity, AtyMainActivity::class.java))
                    finish()
                }

            }

            override fun OnLoadAd(view: View) {
                println(">>>>>>广告加载成功")
                fl_splash.addView(view)
            }
        })
    }

}