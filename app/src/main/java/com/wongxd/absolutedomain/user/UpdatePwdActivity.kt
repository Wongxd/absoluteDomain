package com.wongxd.absolutedomain.user

import android.graphics.Color
import android.os.Bundle
import cn.bmob.v3.BmobUser
import cn.bmob.v3.listener.UpdateListener
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.wongxd.absolutedomain.App
import com.wongxd.absolutedomain.AtyMainActivity
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.SystemUtils
import com.wongxd.absolutedomain.util.TU
import kotlinx.android.synthetic.main.aty_update_pwd.*

/**
 * Created by wongxd on 2017/12/19.
 */
class UpdatePwdActivity : BaseSwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_update_pwd)

        StatusBarUtil.immersive(this)
        StatusBarUtil.setMargin(this, tv_title_aty_update_pwd)

        initView()
    }

    private fun initView() {

        btn_change_aty_update_pwd.setOnClickListener {
            doChangePwd()
        }
    }

    private fun doChangePwd() {
        val old = et_old_pwd_aty_update_pwd.editableText.toString()
        val new = et_new_pwd_aty_update_pwd.editableText.toString()

        val confNew = etconfirm_new_pwd_aty_update_pwd.editableText.toString()

        if (SystemUtils.isHadEmptyText(old, new, confNew)) {
            TU.cT("请完整输入本页信息")
            return
        }

        if (new != confNew) {
            TU.cT("两次新密码输入不一致")
            return
        }

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "修改密码中"
        pDialog.setCancelable(false)
        pDialog.show()

        BmobUser.updateCurrentUserPassword(this, old, new, object : UpdateListener() {
            override fun onSuccess() {
                pDialog.titleText = "修改密码成功，请重新登录"
                pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                pDialog.setConfirmClickListener {
                    pDialog.dismissWithAnimation()
                    BmobUser.logOut(this@UpdatePwdActivity)
                    App.user =  null
                    SystemUtils.cleanTask2Activity(this@UpdatePwdActivity, AtyMainActivity::class.java)
                }

            }

            override fun onFailure(p0: Int, p1: String?) {
                pDialog.contentText = p1
                pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                pDialog.setConfirmClickListener {
                    pDialog.dismissWithAnimation()
                }
            }
        })
    }
}