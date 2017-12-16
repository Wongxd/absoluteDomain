package com.wongxd.absolutedomain


import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.transition.Explode
import com.wongxd.absolutedomain.base.BaseActivity
import com.wongxd.absolutedomain.util.TU
import kotlinx.android.synthetic.main.aty_login.*

/**
 * Created by wongxd on 2017/12/2.
 */
class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_login)

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

    }


    fun loginSuccessed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val explode = Explode()
            explode.duration = 500
            window.exitTransition = explode
            window.enterTransition = explode
            val oc2 = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
            val i2 = Intent(this, AtyMainActivity::class.java)
            startActivity(i2, oc2.toBundle())
        } else
            startActivity(Intent(this, AtyMainActivity::class.java))
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