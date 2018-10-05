package com.papashkin.myrecipes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
//import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
//import android.os.Handler
import android.support.annotation.RequiresApi
//import android.support.v7.app.AppCompatActivity
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class WebBrowserRecipe : Activity() {
    companion object {
        var aTitle = ""
        var anUrl = ""

        @SuppressLint("StaticFieldLeak")
        lateinit var titleView: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var urlView: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var progBar: ProgressBar
    }

    lateinit var mWebView: WebView
    lateinit var menu: PopupMenu

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        anUrl = intent.getStringExtra("URL")
        aTitle = intent.getStringExtra("NAME")

//        titleView = findViewById(R.id.browser_title)
//        titleView.text = aTitle
//        urlView = findViewById(R.id.browser_url)
//        urlView.text = anUrl

        titleView = findViewById(R.id.browser_title)
        titleView.text = aTitle
        urlView = findViewById(R.id.browser_url)
        urlView.text = anUrl

        progBar = findViewById(R.id.browser_progressbar)

        mWebView = findViewById(R.id.recipeView)
        mWebView.settings.javaScriptEnabled = true
        mWebView.webViewClient = MyWebViewClient()
        mWebView.webChromeClient = MyWebChromeClient()
        mWebView.loadUrl(anUrl)
    }

    fun showPopupMenu(v: View){
        menu = PopupMenu(this, v)
        menu.inflate(R.menu.menu_browser)
        menu.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.menu_copy_link ->{
                    copyURL()
                    true
                }
                R.id.menu_share -> {
                    sendURL(mWebView.url)
                    true
                }
                R.id.menu_save_link -> {
                    saveURL()
                    true
                }
                R.id.menu_exit -> {
                    mWebView.destroy()
                    super.onBackPressed()
                    true
                }
                else -> false
            }
        }
        menu.setOnDismissListener {
            it.dismiss()
        }
        menu.show()
    }

    override fun onBackPressed() {
        if (mWebView.canGoBack()){
            mWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mWebView.destroy()
        val intent = Intent(this@WebBrowserRecipe, MainActivity::class.java)
        startActivity(intent)
//        finish()
        super.onDestroy()
    }

    /** @copyURL - function allows to copy the url of current page
     * into clipboard
     */
    private fun copyURL(){
        val newURL = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("copied_text", mWebView.url)
        newURL.primaryClip = clip
        (Toast.makeText(this, "URL was copied", Toast.LENGTH_SHORT)).show()
    }

    /** @sendURL - function for send url of current page
     * to somebody via email, messengers, etc.
     */
    private fun sendURL(url: String){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, url)
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun saveURL() {

    }

    private class MyWebViewClient : WebViewClient(){
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view!!.loadUrl(request!!.url.toString())
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (url!!.isNotEmpty()){
                urlView.text = url
            }
            titleView.text = aTitle
            progBar.visibility = View.INVISIBLE
            super.onPageFinished(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progBar.visibility = View.VISIBLE
            super.onPageStarted(view, url, favicon)
        }

    }

    class MyWebChromeClient : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            if (title!!.isNotEmpty()){
                aTitle = title
            }
            super.onReceivedTitle(view, title)
        }
    }
}