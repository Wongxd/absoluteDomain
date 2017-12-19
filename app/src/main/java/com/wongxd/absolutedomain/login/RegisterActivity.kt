package com.wongxd.absolutedomain.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.CardView
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import cn.bmob.v3.listener.SaveListener
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseActivity
import com.wongxd.absolutedomain.bean.UserBean
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import kotlinx.android.synthetic.main.aty_register.*


/**
 * Created by wongxd on 2017/12/2.
 */
class RegisterActivity : BaseActivity() {


    private lateinit var cvAdd: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_register)
        cvAdd = cv_add_aty_register

        StatusBarUtil.immersive(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ShowEnterAnimation()
        }

        fab_aty_register.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateRevealClose()
            } else {
                cvAdd.setVisibility(View.INVISIBLE)
                fab_aty_register.setImageResource(R.drawable.plus)
                super@RegisterActivity.onBackPressed()
            }
        }

        bt_go_aty_register.setOnClickListener {
            val userName = textlayout_username_register.editText?.text.toString()
            val pwd = textlayout_pwd_register.editText?.text.toString()
            val repeatPwd = textlayout_repeat_pwd_register.editText?.text.toString()
            if (userName.isBlank() || pwd.isBlank() || repeatPwd.isBlank()) {
                TU.cT("本页数据为必填")
                return@setOnClickListener
            }

            if (pwd != repeatPwd) {
                TU.cT("两次密码输入不一致")
                return@setOnClickListener
            }

            doRegister(userName, pwd)
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ShowEnterAnimation() {
        val transition = TransitionInflater.from(this).inflateTransition(R.transition.fabtransition)
        window.sharedElementEnterTransition = transition

        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                cvAdd.setVisibility(View.GONE)
            }

            override fun onTransitionEnd(transition: Transition) {
                transition.removeListener(this)
                animateRevealShow()
            }

            override fun onTransitionCancel(transition: Transition) {

            }

            override fun onTransitionPause(transition: Transition) {

            }

            override fun onTransitionResume(transition: Transition) {

            }


        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun animateRevealShow() {
        val mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd, cvAdd.getWidth() / 2, 0, (fab_aty_register.getWidth() / 2).toFloat(), cvAdd.getHeight().toFloat())
        mAnimator.duration = 500
        mAnimator.interpolator = AccelerateInterpolator()
        mAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }

            override fun onAnimationStart(animation: Animator) {
                cvAdd.setVisibility(View.VISIBLE)
                super.onAnimationStart(animation)
            }
        })
        mAnimator.start()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun animateRevealClose() {
        val mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd, cvAdd.getWidth() / 2, 0, cvAdd.getHeight().toFloat(), (fab_aty_register.getWidth() / 2).toFloat())
        mAnimator.duration = 500
        mAnimator.interpolator = AccelerateInterpolator()
        mAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                cvAdd.setVisibility(View.INVISIBLE)
                super.onAnimationEnd(animation)
                fab_aty_register.setImageResource(R.drawable.plus)
                super@RegisterActivity.onBackPressed()
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
            }
        })
        mAnimator.start()
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateRevealClose()
        }
    }


    fun doRegister(userName: String, pwd: String) {

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "注册中"
        pDialog.setCancelable(false)
        pDialog.show()

        val bu = UserBean()
        bu.username = userName
        bu.setPassword(pwd)
        //注意：不能用save方法进行注册
        bu.signUp(this, object : SaveListener() {
            override fun onSuccess() {
                pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                pDialog.titleText = "注册成功，请登录"
                pDialog.setConfirmClickListener {
                    pDialog.dismissWithAnimation()
                    finish()
                }

            }

            override fun onFailure(p0: Int, p1: String?) {
                pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                pDialog.contentText = p1
                pDialog.setConfirmClickListener {
                    pDialog.dismissWithAnimation()
                }
            }
        })
    }
}