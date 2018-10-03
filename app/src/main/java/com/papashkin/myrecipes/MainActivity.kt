package com.papashkin.myrecipes

//import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var recipes: ArrayList<String>
    private lateinit var ids: ArrayList<Long>
    private lateinit var mContext: Context
    private lateinit var stackAdapter: ArrayAdapter<String>
    private lateinit var scrollRecipe: ListView

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

        btnADD = findViewById(R.id.btn_plus)
        btnADD.visibility = if(fromClipboard != "") {
            View.VISIBLE
        } else View.INVISIBLE
        btnADD.setOnClickListener { addNewRecipe()
        }

        btnDEL = findViewById(R.id.btn_minus)
        btnDEL.setOnClickListener {
            deleteRecipe(selectedRecipeId)
        }
        btnDEL.visibility = View.INVISIBLE

        scrollRecipe = findViewById(R.id.list_recipes)
        recipes = arrayListOf()
        ids = arrayListOf()

        db = RecipeDatabase.getRecipeDatabase(mContext)

        val task = Task_readAllFromDB()
        task.execute(mContext)
        namesAndIds = task.get()
        if (namesAndIds != null) {
            namesAndIds!!.forEach {
                ids.add(it.key)
                recipes.add(it.value)
            }
        }

        stackAdapter = ArrayAdapter(mContext, R.layout.text_resipes, recipes)
        scrollRecipe.adapter = stackAdapter
        scrollRecipe.isLongClickable = true

        scrollRecipe.setOnItemLongClickListener { _, view, position, _ ->
            if (btnDEL.visibility == View.INVISIBLE){
                selectedRecipeId = ids[position].toInt()
                view.alpha = 0.5f
                isInLongClick(false)
            }
            true
        }

        scrollRecipe.setOnItemClickListener { _, view, position, _ ->
            if (btnDEL.visibility == View.INVISIBLE) {
                seeInBrowser(ids[position])
            } else {
                if (view.alpha == 0.5f){
                    view.alpha = 1f
                    isInLongClick(true)
                }
            }
        }
    }

    private fun deleteRecipe(id: Int) {
        if (id != -1) {
            val anID = ids.indexOf(id.toLong())
            deleteFromDB(id.toLong())
            recipes.removeAt(anID)
            ids.removeAt(anID)
            scrollRecipe.adapter = stackAdapter
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
    private fun addNewRecipe() {
        val address =  fromClipboard.toString()
        val parse = ParseTask()
        parse.execute(address)
        val title = parse.get()
        newRecipeWithName(title, address)
    }

    /** @newRecipeWithName - insert new record into DB (using name and url)
     * insertion mechanism is realized in the background via AsyncTask
     */
    private fun newRecipeWithName(name: String, url: String) {
        val checkTask = Task_getIdByAddress()
        checkTask.execute(arrayOf(url, mContext))
        val potentialId = checkTask.get()
        if (potentialId < 0){
            val recipe = Recipe(name, url, "")
            val insertTask = Task_insertToDB()
            insertTask.execute(arrayOf(recipe, mContext))
            val id = insertTask.get()
            ids.add(id)
            stackAdapter.add(name)
            scrollRecipe.adapter = stackAdapter
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
        scrollRecipe.isClickable = bool
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