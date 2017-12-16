package com.wongxd.absolutedomain

import android.os.Bundle
import android.view.MenuItem
import com.wongxd.absolutedomain.base.BaseActivity
import kotlinx.android.synthetic.main.aty_user_info.*

/**
 * Created by wongxd on 2017/12/2.
 */
class UserInfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_user_info)

        setSupportActionBar(toolbar_aty_user_info)
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        actionBar.title = "用户资料"
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}