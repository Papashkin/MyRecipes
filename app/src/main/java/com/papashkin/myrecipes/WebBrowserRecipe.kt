package com.papashkin.myrecipes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
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
        var html_text = ""

        @SuppressLint("StaticFieldLeak")
        lateinit var titleView: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var urlView: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var progBar: ProgressBar
    }

    private lateinit var mWebView: WebView
    private lateinit var menu: PopupMenu
    private lateinit var appContext: Context
    private var isConnected: Boolean = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        appContext = this.applicationContext

        val recipe = intent.getSerializableExtra("RECIPE") as Recipe

        titleView = findViewById(R.id.browser_title)
        titleView.text = recipe.name
        urlView = findViewById(R.id.browser_url)
        urlView.text = recipe.address

        if (savedInstanceState == null){
            html_text = recipe.text
            anUrl = recipe.address
            aTitle = recipe.name
        } else {
            html_text = savedInstanceState.getString("html")
            anUrl = savedInstanceState.getString("address")
            aTitle = savedInstanceState.getString("name")
        }

        progBar = findViewById(R.id.browser_progressbar)

        mWebView = findViewById(R.id.recipeView)
        mWebView.settings.javaScriptEnabled = true
        mWebView.webViewClient = MyWebViewClient()
        mWebView.webChromeClient = MyWebChromeClient()

        isConnected = ifInternetConnection()
        if (isConnected){
            mWebView.loadUrl(anUrl)
        } else {
            mWebView.settings.builtInZoomControls = true
            mWebView.settings.setSupportZoom(true)
            mWebView.settings.useWideViewPort = true
            mWebView.settings.loadWithOverviewMode = true
            mWebView.loadDataWithBaseURL(anUrl, html_text, "text/html",
                    "en_US", null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putString("name", aTitle)
        outState.putString("address", anUrl)
        outState.putString("html", html_text)
        super.onSaveInstanceState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        if (isConnected){
            mWebView.loadUrl(anUrl)
        } else {
            mWebView.loadDataWithBaseURL(anUrl, html_text, "text/html",
                    "en_US", null)
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun ifInternetConnection(): Boolean {
        val connectManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val internet = connectManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state
        val wifi = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state

        return internet == NetworkInfo.State.CONNECTED ||
                wifi == NetworkInfo.State.CONNECTED
    }

    fun showPopupMenu(v: View){
        menu = PopupMenu(this, v)
        menu.inflate(R.menu.menu_browser)
        if (html_text != "") {
            menu.menu.findItem(R.id.menu_save_link).isEnabled = false
        }
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
                    onDestroy()
//                    super.onBackPressed()
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
        val intent = Intent(appContext, MainActivity::class.java)
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
        if (html_text == "") {
            val task = Task_getHTMLpage()
            task.execute(arrayOf(anUrl, appContext))
            val isReady = task.get()
            if (isReady){
                Toast.makeText(appContext, "HTML saved", Toast.LENGTH_SHORT)
                        .show()
            }
        } else {
            Toast.makeText(
                    appContext, "this page is already in Database", Toast.LENGTH_SHORT)
                    .show()
        }

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