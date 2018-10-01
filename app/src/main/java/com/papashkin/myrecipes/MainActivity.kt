package com.papashkin.myrecipes

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var recipes: ArrayList<String>
    private lateinit var ids: ArrayList<Long>
    private lateinit var mContext: Context
    private lateinit var stackAdapter: ArrayAdapter<String>
    private lateinit var svRecipes: StackView
    private lateinit var btnADD: ImageButton
    private lateinit var btnDEL: ImageButton
    private lateinit var db: RecipeDatabase

    private var namesAndIds: Map<Long, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = applicationContext

        btnADD = findViewById(R.id.btn_plus)
        btnADD.setOnClickListener { addNewRecipe()
        }

        btnDEL = findViewById(R.id.btn_minus)
        btnDEL.setOnClickListener { deleteRecipe()
        }

        svRecipes = findViewById(R.id.stackview_resipes)
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
        } else {
            recipes = arrayListOf()
            ids = arrayListOf()
        }
        
        stackAdapter = ArrayAdapter(mContext, R.layout.text_resipes, recipes)
        svRecipes.adapter = stackAdapter
        svRecipes.setOnItemClickListener { _, _, position, _ ->
            seeInBrowser(ids[position])
        }
    }

    private fun deleteRecipe() {
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.dialog_deleterecipe)
        dialog.setTitle("Select the recipe")

        val btnOK = dialog.findViewById<ImageButton>(R.id.btn_done)
        val recipeList = dialog.findViewById<Spinner>(R.id.spinner_recipes)
        val list = ArrayAdapter(mContext,
                R.layout.text_recipes_for_delete, recipes)
        recipeList.adapter = list

        btnOK.setOnClickListener {
            val selectedRecipe = recipeList.selectedItem.toString()
            val selectedID = ids[recipes.indexOf(selectedRecipe)]
            deleteFromDB(selectedID)
            recipes.remove(selectedRecipe)
            ids.remove(selectedID)
            recipeList.adapter = list
            svRecipes.adapter = stackAdapter
            (Toast.makeText(mContext, "Selected recipe was deleted", Toast.LENGTH_SHORT)).show()
        }
        dialog.show()
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

    /** @addNewRecipe allows insert new record to DB using the name of the recipe and url.
     * Also can use only the url - the system uses the title of the web page as a name.
     */
    private fun addNewRecipe() {
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.dialog_newrecipe)

        val btnOK = dialog.findViewById<ImageButton>(R.id.btn_done)
        val btnCANCEL = dialog.findViewById<ImageButton>(R.id.btn_cancel)
        val newRecipeName = dialog.findViewById<EditText>(R.id.edittext_recipe_name)
        val newRecipeUrl = dialog.findViewById<EditText>(R.id.edittext_url)

        val fromClipboard = getFromClipboard()
        newRecipeUrl.setText(fromClipboard)

        btnOK.setOnClickListener {
            val name = newRecipeName.editableText.toString()
            val address = newRecipeUrl.editableText.toString()
            when {
                address == "" -> {
                    (Toast.makeText(this, "Incorrect data", Toast.LENGTH_SHORT)).show()
                }
                name == "" -> {
                    dialog.dismiss()
                    newRecipeWithoutName(address)
                }
                else -> {
                    dialog.dismiss()
                    newRecipeWithName(name, address)
                }
            }
        }
        btnCANCEL.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /** @newRecipeWithoutName - insert new record into DB (using only url)
     * title is taken from html code in AsyncTask, then start the "newRecipeWithName" function
     */
    private fun newRecipeWithoutName(url: String) {
        val parse = ParseTask()
        parse.execute(url)
        val title = parse.get()
        newRecipeWithName(title, url)
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
            svRecipes.adapter = stackAdapter
            (Toast.makeText(mContext, "Recipe was added", Toast.LENGTH_SHORT)).show()
        } else {
            (Toast.makeText(mContext, "This link is already exists in DB", Toast.LENGTH_SHORT)).show()
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
}