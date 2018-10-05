package com.papashkin.myrecipes

import android.content.ClipboardManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
//import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.jsoup.Jsoup
import java.lang.Exception

//@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    var isReady = false
    var progressStatus = 0
//    val handler = Handler()

    private lateinit var recipes: ArrayList<String>
    private lateinit var ids: ArrayList<Long>
    private lateinit var mContext: Context

    private lateinit var rvRecipes: RecyclerView
    private lateinit var llm: RecyclerView.LayoutManager
    private lateinit var imgUrls: ArrayList<String>
    private lateinit var recipeList: ArrayList<Recipe>
    private lateinit var rvAdapter: RVAdapter

    private lateinit var progressBar: ProgressBar
    private lateinit var btnAdd: Button

    private lateinit var fromClipboard: CharSequence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        mContext = applicationContext

        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = progressStatus
        progressBar.visibility = View.VISIBLE

        btnAdd = findViewById(R.id.btn_newRecipe)
        rvRecipes = findViewById(R.id.rv_recipes)
        llm = LinearLayoutManager(mContext)
        rvRecipes.layoutManager = llm

        fromClipboard = getFromClipboard()

        recipes = arrayListOf()
        ids = arrayListOf()
        imgUrls = arrayListOf()

//        val checker = Task_checkEmptyRecords()
//        checker.execute(mContext)

        Thread(Runnable {
            val db = RecipeDatabase.getRecipeDatabase(mContext)
            val list_address = db.recipeDao().addresses
            val list_recipes = db.recipeDao().names
            val list_img = db.recipeDao().imageUrls
            val list_ids = db.recipeDao().iDs
            for (i in list_img.indices){
                recipes.add(list_recipes[i])
                ids.add(list_ids[i])
                if (list_img[i] == null){
                    try {
                        val doc = Jsoup.connect(list_address[i]).get()
                        val elements = doc.select("meta")
                        elements.forEach {
                            val prop = it.attr("property")
                            if (prop == "og:image"){
                                val imgurl = it.attr("content")
                                db.recipeDao().updImageUrl(imgurl,list_ids[i])
                                imgUrls.add(imgurl)
                            }
                        }
                    } catch (ex: Exception){
                        ex.printStackTrace()
                    }
                } else imgUrls.add(list_img[i])
            }
            db.close()
            isReady = !isReady
        }).start()

        do {
            Thread.sleep(200)
        } while (!isReady)

        initializationData()
        initializationAdapter()
        progressBar.visibility = View.INVISIBLE
    }

    private fun initializationAdapter() {
        val indexes = arrayListOf<Int>()
        ids.forEach {
            indexes.add(it.toInt())
        }
        rvAdapter = RVAdapter(recipes, imgUrls, indexes)
        rvRecipes.adapter = rvAdapter
    }

    private fun initializationData() {
        recipeList = arrayListOf()
        for (i in recipes.indices){
            recipeList.add(Recipe(recipes[i], imgUrls[i]))
        }
    }

//    private fun deleteRecipe(id: Int) {
//        if (id != -1) {
//            val anID = ids.indexOf(id.toLong())
//            deleteFromDB(id.toLong())
//            recipes.removeAt(anID)
//            ids.removeAt(anID)
//            listRecipe.adapter = stackAdapter
//            isInLongClick(true)
//        }
//    }

    /** @deleteFromDB allows delete one selected record from DB
     * (using selectedID for searching)
     */
//    private fun deleteFromDB(selectedID: Long) {
//        val task = Task_deleteFromDB()
//        task.execute(arrayOf(selectedID, mContext))
//        val isDeleted = task.get()
//        if (isDeleted){
//            (Toast.makeText(mContext, "Selected recipe was deleted", Toast.LENGTH_SHORT)).show()
//        }
//    }

    /** @addNewRecipe allows insert new record to DB using url.
     *  System uses the title of the web page as a name.
     */
//    private fun addNewRecipe() {
    fun addNewRecipe(v: View) {
        val address =  fromClipboard.toString()
        if (address.contains("http")){
            val titleParser = Task_getTitle()
            titleParser.execute(address)
            val title = titleParser.get()
            newRecipeWithName(title, address)
        } else {
            Toast.makeText(mContext, "This is not an URL", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /** @newRecipeWithName - insert new record into DB (using name and url)
     * insertion mechanism is realized in the background via AsyncTask
     */
    private fun newRecipeWithName(name: String, url: String) {
        val imgParser = Task_getImageUrl()
        imgParser.execute(url)
        val imgUrl = imgParser.get()
        val checkTask = Task_getIdByAddress()
        checkTask.execute(arrayOf(url, mContext))
        val potentialId = checkTask.get()
        if (potentialId < 0){
            val recipe = Recipe(name, url, "", imgUrl)
            val insertTask = Task_insertToDB()
            insertTask.execute(arrayOf(recipe, mContext))
            val id = insertTask.get()
            recipes.add(name)
            imgUrls.add(imgUrl)
            ids.add(id)
            rvAdapter.notifyDataSetChanged()
            Toast.makeText(mContext, "Recipe was added", Toast.LENGTH_SHORT)
                    .show()
        } else {
            Toast.makeText(mContext, "This link was already added", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /** @seeInBrowser allows see the saved url in the build-in browser
     */
//    private fun seeInBrowser(anID: Long) {
//        val task = Task_getNameAndAddressById()
//        task.execute(arrayOf(anID, mContext))
//        val name = task.get()[0]
//        val address = task.get()[1]
//
//        if (address != "") {
//            val intent = Intent(mContext, WebBrowserRecipe::class.java)
//            intent.putExtra("URL", address)
//            intent.putExtra("NAME", name)
//            finish()
//            startActivity(intent)
//        } else (Toast.makeText(mContext, "Reading failed", Toast.LENGTH_SHORT)).show()
//    }

    /** @getFromClipboard takes the url (starts from "http...") from clipboard
     * and inserts it into the "url" field in the dialog.
     */
    private fun getFromClipboard(): CharSequence {
        var address: CharSequence = ""
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE)
                as ClipboardManager
        val clipData = clipboardManager.primaryClip

        if (clipData != null) {
            val clipText = clipData.getItemAt(0).text
            if (clipText.contains("http")) {
                address = clipText
            }
        }
        return address
    }

//    private fun isInLongClick(bool: Boolean){
//        btnADD.isClickable = bool
//        listRecipe.isClickable = bool
//        if (bool){
//            btnADD.alpha = 1f
//            btnDEL.visibility = View.INVISIBLE
//            selectedRecipeId = -1
//        } else {
//            btnADD.alpha = 0.5f
//            btnDEL.visibility = View.VISIBLE
//        }
//    }
}