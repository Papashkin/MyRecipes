package com.papashkin.myrecipes

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class WebBrowserRecipe : AppCompatActivity() {

    companion object {
        var myTitle: String = ""
    }
    private lateinit var mWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        val address = intent.getStringExtra("URL")
        myTitle = intent.getStringExtra("NAME")

        title = myTitle
        mWebView = findViewById(R.id.recipeView)
        mWebView.settings.javaScriptEnabled = true
        mWebView.webViewClient = MyWebViewClient()
        mWebView.webChromeClient = MyWebChromeClient()
        mWebView.loadUrl(address)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.menu_browser, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId){
            R.id.menu_copy_link -> {
                copyURL()
                true
            }
            R.id.menu_share -> {
                sendURL(mWebView.url)
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

    private class MyWebViewClient : WebViewClient(){
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view!!.loadUrl(request!!.url.toString())
            return true
        }
    }

    class MyWebChromeClient : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            myTitle = view!!.title
            super.onReceivedTitle(view, title)
        }
    }
}