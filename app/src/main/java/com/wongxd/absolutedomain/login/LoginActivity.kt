package com.wongxd.absolutedomain.login


import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.transition.Explode
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.wongxd.absolutedomain.AtyMainActivity
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.bean.UserBean
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import kotlinx.android.synthetic.main.aty_login.*


/**
 * Created by wongxd on 2017/12/2.
 */
class LoginActivity : BaseSwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_login)

        StatusBarUtil.immersive(this)

        btn_go_aty_login.setOnClickListener {

            val userName = textlayout_username_login.editText?.text.toString()

            val pwd = textlayout_pwd_login.editText?.text.toString()

            if (pwd.isBlank() || userName.isBlank()) {
                TU.cT("用户名和密码为必填")
                return@setOnClickListener
            }

            doLogin(userName, pwd)

        }

        fab_aty_login.setOnClickListener { doRegister() }
    }

    fun doLogin(userName: String, pwd: String) {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "登录中"
        pDialog.setCancelable(false)
        pDialog.show()
        BmobUser.loginByAccount(this, userName, pwd, object : LogInListener<UserBean>() {
            override fun done(p0: UserBean?, p1: BmobException?) {
                if (p0 != null) {
                    pDialog.titleText = "你好---$userName"
                    pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    pDialog.setConfirmClickListener {
                        pDialog.dismissWithAnimation()
                        loginSuccessed()
                    }

                } else {
                    pDialog.contentText = p1?.message
                    pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                    pDialog.setConfirmClickListener {
                        pDialog.dismissWithAnimation()
                    }
                }
            }

        })
    }


    fun loginSuccessed() {

        val i2 = Intent(this, AtyMainActivity::class.java)
        i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val explode = Explode()
            explode.duration = 500
            window.exitTransition = explode
            window.enterTransition = explode
            val oc2 = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
            startActivity(i2, oc2.toBundle())
        } else
            startActivity(i2)
        finish()
    }


    private fun doRegister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.exitTransition = null
            window.enterTransition = null
            val options = ActivityOptions.makeSceneTransitionAnimation(this, fab_aty_login, fab_aty_login.getTransitionName())
            startActivity(Intent(this, RegisterActivity::class.java), options.toBundle())
        } else {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}