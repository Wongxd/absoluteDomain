package com.wongxd.absolutedomain.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.wongxd.absolutedomain.BuildConfig
import com.wongxd.absolutedomain.base.BaseSwipeActivity
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.webView
import java.util.*


class WebViewActivity : BaseSwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra("url") ?: "https://qr.alipay.com/c1x03491e5pr1lnuoid3e22"

        verticalLayout {
            webView() {
                initWebView(this, this@WebViewActivity, url)
            }
        }


    }
}


fun initWebView(mWeb: WebView, ctx: AppCompatActivity, url: String) {
    val UA = ("Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; "
            + Locale.getDefault().language + "-" + Locale.getDefault().country.toLowerCase()
            + "; Nexus 5 Build/JOP40D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36")
    mWeb.settings.allowFileAccess = true
    mWeb.settings.javaScriptEnabled = true
    mWeb.settings.useWideViewPort = true
    mWeb.settings.loadWithOverviewMode = true
/*
 * fixed issues #12
 * http://stackoverflow.com/questions/9476151/webview-flashing-with-white-background-if-hardware-acceleration-is-enabled-an
 */
    if (Build.VERSION.SDK_INT >= 11)
        mWeb.setBackgroundColor(Color.argb(1, 0, 0, 0))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }
    mWeb.webChromeClient = WebChromeClient()
    mWeb.settings.userAgentString = UA
    mWeb.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//            val uri = Uri.parse(url)
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.addCategory(Intent.CATEGORY_DEFAULT)
//            intent.data = uri

            val intent = Intent.parseUri(url,
                    Intent.URI_INTENT_SCHEME);
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.component = null;
//            if ("alipayqr" == uri.getScheme() || "alipays".endsWith(uri.getScheme())) {
//                try {
//                    ctx.startActivity(intent)
//                    ctx.finish()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//                return true
//            } else if (url.startsWith("http") && url.endsWith(".apk")) {
//                ctx.startActivity(intent)
//                return true
//            } else if (url.contains("QR.ALIPAY.COM")) { //HTTPS://QR.ALIPAY.COM/FKX07373TRZS7EQ7SUVI9A
                ctx.startActivity(intent)
                return true
//            }
//            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    mWeb.loadUrl(url)
}