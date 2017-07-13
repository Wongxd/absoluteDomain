package com.wongxd.absolutedomain.ui.aty

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.orhanobut.logger.Logger
import com.wongxd.absolutedomain.AtyMainActivity
import com.wongxd.absolutedomain.R
import com.wongxd.absolutedomain.base.BaseActivity
import com.wongxd.absolutedomain.base.exception.AppManager
import com.wongxd.absolutedomain.bean.ThemeBean
import com.wongxd.absolutedomain.util.ConfigUtils
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.aty_theme.*

class ThemeActivity : BaseActivity() {

    var last = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_theme)
        last = ConfigUtils.getTheme(applicationContext)
        rv_theme.layoutManager = LinearLayoutManager(applicationContext)
        rv_theme.itemAnimator = LandingAnimator()

        val adapter = ThemeAdapter()
        rv_theme.adapter = adapter
        adapter.setNewData(initTheme())
    }

    private fun initTheme(): List<ThemeBean> {
        val themes = java.util.ArrayList<ThemeBean>()
//        themes.add(ThemeBean(R.style.ThemeDarkBlack, R.color.ThemeDarkBlack, "夜间/Night", false))
        themes.add(ThemeBean(R.style.ThemePurple, R.color.ThemePurple, "紫色/Purple", false))
        themes.add(ThemeBean(R.style.ThemeBiliBili, R.color.ThemeBiliBili, "哔哩哔哩/BiliBili", false))
        themes.add(ThemeBean(R.style.ThemeDeepPurple, R.color.ThemeDeepPurple, "深紫/Deep Purple", false))
        themes.add(ThemeBean(R.style.ThemeGreen, R.color.ThemeGreen, "绿色/Green", false))
        themes.add(ThemeBean(R.style.ThemeRed, R.color.ThemeRed, "红色/Red", false))
        themes.add(ThemeBean(R.style.ThemePink, R.color.ThemePink, "粉色/Pink", false))
        themes.add(ThemeBean(R.style.ThemeIndigo, R.color.ThemeIndigo, "靛蓝/Indigo", false))
        themes.add(ThemeBean(R.style.ThemeBlue, R.color.ThemeBlue, "蓝色/Blue", false))
        themes.add(ThemeBean(R.style.ThemeLightBlue, R.color.ThemeLightBlue, "亮蓝/Light Blue", false))
        themes.add(ThemeBean(R.style.ThemeCyan, R.color.ThemeCyan, "青色/Cyan", false))
        themes.add(ThemeBean(R.style.ThemeTeal, R.color.ThemeTeal, "鸭绿/Teal", false))
        themes.add(ThemeBean(R.style.ThemeLightGreen, R.color.ThemeLightGreen, "亮绿/Light Green", false))
        themes.add(ThemeBean(R.style.ThemeLime, R.color.ThemeLime, "酸橙/Lime", false))
        themes.add(ThemeBean(R.style.ThemeYellow, R.color.ThemeYellow, "黄色/Yellow", false))
        themes.add(ThemeBean(R.style.ThemeAmber, R.color.ThemeAmber, "琥珀/Amber", false))
        themes.add(ThemeBean(R.style.ThemeOrange, R.color.ThemeOrange, "橙色/Orange", false))
        themes.add(ThemeBean(R.style.ThemeDeepOrange, R.color.ThemeDeepOrange, "暗橙/DeepOrange", false))
        themes.add(ThemeBean(R.style.ThemeBrown, R.color.ThemeBrown, "棕色/Brown", false))
        themes.add(ThemeBean(R.style.ThemeGrey, R.color.ThemeGrey, "灰色/Grey", false))
        themes.add(ThemeBean(R.style.ThemeBlueGrey, R.color.ThemeBlueGrey, "蓝灰/Blue Grey", false))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            themes.add(ThemeBean(R.style.ThemeBlack, R.color.ThemeBlack, "黑色/Black", false))
        }

        themes
                .filter { it.theme == ConfigUtils.getTheme(this) }
                .forEach { it.isSelected = true }
        return themes
    }

    override fun onBackPressed() {
        Logger.e(ConfigUtils.getTheme(applicationContext).toString() +"  "+ last.toString())
        if (ConfigUtils.getTheme(applicationContext) == last) finish()
        else {
            AppManager.getAppManager().finishActivity(AtyMainActivity::class.java)
            startActivity(Intent(this, AtyMainActivity::class.java))
            finish()
        }
    }

    class ThemeAdapter : BaseQuickAdapter<ThemeBean, BaseViewHolder>(R.layout.item_rv_theme) {
        override fun convert(helper: BaseViewHolder?, item: ThemeBean?) {
            if (item?.isSelected!!) {
                helper?.getView<ImageView>(R.id.iv_theme_image)?.setImageResource(R.drawable.ic_item_theme_checked)
            } else {
                helper?.getView<ImageView>(R.id.iv_theme_image)?.setImageResource(R.drawable.ic_item_theme_uncheck)
            }
            helper?.setVisible(R.id.tv_state_flag, item.isSelected)
            helper?.getView<TextView>(R.id.tv_state_flag)?.setTextColor(ContextCompat.getColor(mContext, item.color))
            helper?.getView<ImageView>(R.id.iv_theme_image)?.setColorFilter(ContextCompat.getColor(mContext, item.color))
            helper?.setText(R.id.tv_theme_description, item.description)
            helper?.itemView?.setOnClickListener({
                for (bean in data) {
                    bean.isSelected = false
                }
                item.isSelected = true
                ConfigUtils.saveTheme(mContext, item.theme)
                notifyDataSetChanged()
            })
        }
    }

}
