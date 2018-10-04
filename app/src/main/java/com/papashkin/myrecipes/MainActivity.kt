package com.papashkin.myrecipes

//import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    var isReady = false

    private lateinit var recipes: ArrayList<String>
    private lateinit var ids: ArrayList<Long>
    private lateinit var mContext: Context
    private lateinit var stackAdapter: ArrayAdapter<String>

    private lateinit var listRecipe: ListView
    private lateinit var rvRecipes: RecyclerView
    private lateinit var llm: RecyclerView.LayoutManager
    private lateinit var imgUrls: ArrayList<String>
    private lateinit var recipeList: ArrayList<Recipe>
    private lateinit var rvAdapter: RVAdapter

    private lateinit var btnADD: ImageButton
    private lateinit var btnDEL: ImageButton
    private lateinit var db: RecipeDatabase
    private lateinit var fromClipboard: CharSequence

    private var selectedRecipeId = -1
    private var namesAndIds: Map<Long, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = applicationContext
        fromClipboard = getFromClipboard()

//        btnADD = findViewById(R.id.btn_plus)
//        btnADD.visibility = if(fromClipboard != "") {
//            View.VISIBLE
//        } else View.INVISIBLE
//        btnADD.setOnClickListener { addNewRecipe()
//        }
//
//        btnDEL = findViewById(R.id.btn_minus)
//        btnDEL.setOnClickListener {
//            deleteRecipe(selectedRecipeId)
//        }
//        btnDEL.visibility = View.INVISIBLE

//        listRecipe = findViewById(R.id.list_recipes)

        rvRecipes = findViewById(R.id.rv_recipes)
        llm = LinearLayoutManager(mContext)
        rvRecipes.layoutManager = llm

        recipes = arrayListOf()
        ids = arrayListOf()
        imgUrls = arrayListOf()

        db = RecipeDatabase.getRecipeDatabase(mContext)

        val checker = Task_checkEmptyRecords()
        checker.execute(mContext)

        val task = Task_readAllFromDB()
        task.execute(mContext)
        namesAndIds = task.get()
        if (namesAndIds != null) {
            namesAndIds!!.forEach {
                ids.add(it.key)
                recipes.add(it.value)
            }
        }

        val runnable = Runnable{
            ids.forEach {
                val addressGetter = Task_getAddressById()
                addressGetter.execute(arrayOf(it, mContext))
                val address = addressGetter.get()
                val imgUrlGetter = Task_getImageUrl()
                imgUrlGetter.execute(address)
                imgUrls.add(imgUrlGetter.get())
            }
            isReady = true
        }
        val thread = Thread(runnable)
        thread.start()

        do {
            Thread.sleep(500)
        } while (!isReady)

        initializationData()
        initializationAdapter()

        rvRecipes.setOnClickListener {
            seeInBrowser(ids[it.id])
        }

//        stackAdapter = ArrayAdapter(mContext, R.layout.text_resipes, recipes)
//        listRecipe.adapter = stackAdapter
//        listRecipe.isLongClickable = true

//        listRecipe.setOnItemLongClickListener { _, view, position, _ ->
//            if (btnDEL.visibility == View.INVISIBLE){
//                selectedRecipeId = ids[position].toInt()
//                view.alpha = 0.5f
//                isInLongClick(false)
//            }
//            true
//        }

//        listRecipe.setOnItemClickListener { _, view, position, _ ->
//            if (btnDEL.visibility == View.INVISIBLE) {
//                seeInBrowser(ids[position])
//            } else {
//                if (view.alpha == 0.5f){
//                    view.alpha = 1f
//                    isInLongClick(true)
//                }
//            }
//        }
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
//        Thread.sleep(4000)
        recipeList = arrayListOf()
        for (i in recipes.indices){
            recipeList.add(Recipe(recipes[i], imgUrls[i]))
        }
    }

    private fun deleteRecipe(id: Int) {
        if (id != -1) {
            val anID = ids.indexOf(id.toLong())
            deleteFromDB(id.toLong())
            recipes.removeAt(anID)
            ids.removeAt(anID)
            listRecipe.adapter = stackAdapter
            isInLongClick(true)
        }
    }

    /** @deleteFromDB allows delete one selected record from DB
     * (using selectedID for searching)
     */
    private fun deleteFromDB(selectedID: Long) {
        val task = Task_deleteFromDB()
        task.execute(arrayOf(selectedID, mContext))
        val isDeleted = task.get()
        if (isDeleted){
            (Toast.makeText(mContext, "Selected recipe was deleted", Toast.LENGTH_SHORT)).show()
        }
    }

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
//            ids.add(id)
//            stackAdapter.add(name)
//            listRecipe.adapter = stackAdapter
            recipes.add(name)
            imgUrls.add(imgUrl)
            ids.add(id)
//            rvAdapter = RVAdapter(recipes, imgUrls, ids)
//            rvRecipes.adapter = rvAdapter

            (Toast.makeText(mContext, "Recipe was added", Toast.LENGTH_SHORT)).show()
        } else {
            (Toast.makeText(mContext, "This link was already added", Toast.LENGTH_SHORT)).show()
        }
    }

    /** @seeInBrowser allows see the saved url in the build-in browser
     */
    private fun seeInBrowser(anID: Long) {
        val task = Task_getNameAndAddressById()
        task.execute(arrayOf(anID, mContext))
        val name = task.get()[0]
        val address = task.get()[1]

        if (address != "") {
            val intent = Intent(mContext, WebBrowserRecipe::class.java)
            intent.putExtra("URL", address)
            intent.putExtra("NAME", name)
            finish()
            startActivity(intent)
        } else (Toast.makeText(mContext, "Reading failed", Toast.LENGTH_SHORT)).show()
    }

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

    private fun isInLongClick(bool: Boolean){
        btnADD.isClickable = bool
        listRecipe.isClickable = bool
        if (bool){
            btnADD.alpha = 1f
            btnDEL.visibility = View.INVISIBLE
            selectedRecipeId = -1
        } else {
            btnADD.alpha = 0.5f
            btnDEL.visibility = View.VISIBLE
        }
    }
}