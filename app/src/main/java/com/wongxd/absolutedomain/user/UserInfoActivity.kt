package com.wongxd.absolutedomain.user

import android.os.Bundle
import cn.bmob.v3.BmobUser
import com.wongxd.absolutedomain.App
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import com.wongxd.absolutedomain.base.rx.RxBus
import com.wongxd.absolutedomain.base.rx.RxEventCodeType
import com.wongxd.absolutedomain.util.StatusBarUtil
import com.wongxd.absolutedomain.util.TU
import kotlinx.android.synthetic.main.aty_user_info.*
import org.jetbrains.anko.startActivity

/**
 * Created by wongxd on 2017/12/2.
 */
class UserInfoActivity : BaseSwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_user_info)

        StatusBarUtil.immersive(this)
        StatusBarUtil.setMargin(this, tv_title_aty_user_info)

        initView()
    }


    fun initView() {

        App.user?.let {

            tv_user_name_aty_user_info.text = it.username
            btn_logout_aty_user_info.setOnClickListener { logOut() }

            ll_update_pwd_aty_user_info.setOnClickListener { updatePwd() }
        }
    }

    private fun updatePwd() {
      startActivity<UpdatePwdActivity>()
    }

    fun logOut() {
        BmobUser.logOut(this)
        TU.cT("已经退出账户")
        App.user = null
        RxBus.getDefault().post(RxEventCodeType.LOGOUT, "logout")
        finish()
    }
}