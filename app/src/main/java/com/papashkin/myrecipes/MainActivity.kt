package com.papashkin.myrecipes

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.*
import org.jsoup.Jsoup
import java.lang.Exception
import android.graphics.RectF
import android.os.Parcelable
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {
    private var isReady = false
    private val paint = Paint()
    val MSG_EDIT = "Insert new recipe name"

    private lateinit var recipeList: ArrayList<Recipe>

    private lateinit var mContext: Context

    private lateinit var rvRecipes: RecyclerView
    private lateinit var llm: RecyclerView.LayoutManager
    private lateinit var rvAdapter: RVAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAdd: ImageButton
    private lateinit var mBundle: Bundle

    private lateinit var newTitle: String

    private lateinit var iconDel: Bitmap
    private lateinit var iconEdit: Bitmap

    private lateinit var fromClipboard: CharSequence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        mContext = applicationContext

        progressBar = findViewById(R.id.main_progressBar)
        btnAdd = findViewById(R.id.btn_newRecipe)
        rvRecipes = findViewById(R.id.rv_recipes)
        rvRecipes.visibility = View.INVISIBLE
        llm = LinearLayoutManager(mContext)
        rvRecipes.layoutManager = llm

        fromClipboard = getFromClipboard()

        recipeList = arrayListOf()

//        val checker = Task_checkEmptyRecords()
//        checker.execute(mContext)

        initializationData()
        initializationAdapter()
        rvRecipes.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
    }

//    override fun onDestroy() {
//        super.onDestroy()
//    }

    override fun onPause() {
        mBundle = Bundle()
        mBundle.putSerializable("LIST", recipeList)
        super.onPause()
    }

    override fun onRestart() {
//        val a = mBundle.getSerializable("LIST")
        if (!mBundle.isEmpty){
            recipeList = mBundle.getSerializable("LIST") as ArrayList<Recipe>
            initializationAdapter()
        }
        super.onRestart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.menu_main_search -> {
                true
            }
            R.id.menu_main_exit -> {
                finish()
                true
            }
            else -> false
        }
    }

    private fun initializationData() {
        progressBar.visibility = View.VISIBLE
        Thread(Runnable {
            val db = RecipeDatabase.getRecipeDatabase(mContext)
            val recipesTest = db.recipeDao().all
            recipesTest.forEach {
                if (it.imageUrl == null){
                    var imgurl = ""
                    try {
                        val id = it.id
                        val doc = Jsoup.connect(it.address).get()
                        val elements = doc.select("meta")
                        elements.forEach {
                            val prop = it.attr("property")
                            if (prop == "og:image"){
                                imgurl = it.attr("content")
                                db.recipeDao().updImageUrl(imgurl, id)
                            }
                        }
                    } catch (ex: Exception){
                        ex.printStackTrace()
                    }
                    if (imgurl.isNotEmpty()) it.imageUrl = imgurl
                }
                recipeList.add(it)
            }
            db.close()
            isReady = !isReady
        }).start()

        do {
            Thread.sleep(100)
        } while (!isReady)
        progressBar.visibility = View.INVISIBLE
    }

    private fun initializationAdapter() {
        rvAdapter = RVAdapter(recipeList)
        rvRecipes.adapter = rvAdapter
        initSwipe()
    }

    private fun initSwipe() {
        val dragFlag = 0
        val swipeFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(dragFlag,swipeFlag) {

            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT){
                    val id = recipeList[pos].id
                    deleteFromDB(id)
                    rvAdapter.removeItem(pos)
                } else {
                    initDialog(pos)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                                     viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                                     actionState: Int, isCurrentlyActive: Boolean) {
                iconDel = BitmapFactory.decodeResource(
                        mContext.resources, R.drawable.ic_delete_white_36dp)
                iconEdit = BitmapFactory.decodeResource(
                        mContext.resources, R.drawable.ic_mode_edit_white_36dp)

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    val itemView = viewHolder.itemView
                    val height = (itemView.bottom - itemView.top).toFloat()
                    val width = height/3

                    if (dX > 0) {
                        paint.color = Color.parseColor("#388E3C")
                        val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                        c.drawRect(background, paint)

                        val icon_dest = RectF(itemView.left + width , itemView.top + width, itemView.left + 2*width, itemView.bottom - width)
                        c.drawBitmap(iconEdit, null, icon_dest, paint)
                    } else {
                        paint.color = Color.parseColor("#C62828")
                        val background = RectF(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(background, paint)

                        val icon_dest = RectF(itemView.right - 2*width , itemView.top + width, itemView.right - width, itemView.bottom - width)
                        c.drawBitmap(iconDel, null, icon_dest, paint)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                        actionState, isCurrentlyActive)
            }
        }
        val touchHelper = ItemTouchHelper(simpleCallback)
        touchHelper.attachToRecyclerView(rvRecipes)
    }

    private fun initDialog(id: Int) {
        newTitle = ""
        val dialog = Dialog(this@MainActivity)
        dialog.setTitle(MSG_EDIT)
        dialog.setContentView(R.layout.dialog_newtitle)
        val et = dialog.findViewById<EditText>(R.id.et_recipe)
        et.setText(recipeList[id].name, TextView.BufferType.EDITABLE)
        val btnSET = dialog.findViewById<Button>(R.id.btnOK)
        btnSET.setOnClickListener {
            newTitle = et.text.toString()
                    if (newTitle == "") {
                        Toast.makeText(mContext, "incorrect name", Toast.LENGTH_SHORT)
                                .show()
                    } else {
                        val taskUpdater = Task_newTitle()
                        taskUpdater.execute(arrayOf(newTitle, recipeList[id].id, mContext))
                        val isOk = taskUpdater.get()
                        rvAdapter.changeTitle(newTitle, id)
                        dialog.dismiss()
                    }
        }
        val btnCANCEL = dialog.findViewById<Button>(R.id.btnCANCEL)
        btnCANCEL.setOnClickListener {
            rvAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.setOnDismissListener {
            rvAdapter.notifyDataSetChanged()
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

    /** @addNewRecipe allows insert new record to DB using url.
     *  System uses the title of the web page as a name.
     */
    fun addNewRecipe(v: View) {
        progressBar.visibility = View.VISIBLE
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
        progressBar.visibility = View.INVISIBLE
    }

    /** @newRecipeWithName - insert new record into DB (using name and url)
     * insertion mechanism is realized in the background via AsyncTask
     */
    private fun newRecipeWithName(name: String, url: String) {
        progressBar.visibility = View.VISIBLE
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
            recipe.id = id
            rvAdapter.addItem(recipe)
            Toast.makeText(mContext, "New recipe was added", Toast.LENGTH_SHORT)
                    .show()
        } else {
            Toast.makeText(mContext, "This recipe exists in your database", Toast.LENGTH_SHORT)
                    .show()
        }
        progressBar.visibility = View.INVISIBLE
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

    /** @showInWeb allows see the saved url in the build-in browser
     */
    fun showInWeb(v: View){
        val ids = arrayListOf<Long>()
        recipeList.forEach {
            ids.add(it.id)
        }
        val id = ids.indexOf(v.id.toLong())
        val address = recipeList[id].address
        val title = recipeList[id].name

        if (address != "") {
            val intent = Intent(mContext, WebBrowserRecipe::class.java)
            intent.putExtra("URL", address)
            intent.putExtra("NAME", title)
//            finish()
            onPause()
            startActivity(intent)
        } else {
            Toast.makeText(mContext, "page URL is absent \n Page loading failed", Toast.LENGTH_SHORT)
                    .show()
        }
    }
}