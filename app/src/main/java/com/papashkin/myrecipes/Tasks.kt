package com.papashkin.myrecipes

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.jsoup.Jsoup
import java.lang.Exception

class Task_getIdByAddress: AsyncTask<Array<Any>, Void, Long>(){
    override fun doInBackground(vararg params: Array<Any>): Long {
        val address = params[0][0] as String
        val context = params[0][1] as Context
        val db = RecipeDatabase.getRecipeDatabase(context)
        val id = db.recipeDao().getIdByAddress(address)
        db.close()
        return id ?: -1L    //  return id
    }
}

class Task_insertToDB: AsyncTask<Array<Any>, Void, Long>(){
    override fun doInBackground(vararg params: Array<Any>): Long {
        val recipe = params[0][0] as Recipe
        val context = params[0][1] as Context
        val db = RecipeDatabase.getRecipeDatabase(context)
        val id = db.recipeDao().insert(recipe)
        db.close()
        return id
    }
}

class Task_deleteFromDB: AsyncTask<Array<Any>, Void, Boolean>(){
    override fun doInBackground(vararg params: Array<Any>): Boolean {
        val id = params[0][0] as Long
        val context = params[0][1] as Context
        val db = RecipeDatabase.getRecipeDatabase(context)
        val recipe = db.recipeDao().getAllById(id)
        db.recipeDao().delete(recipe[0])
        db.close()
        return true
    }
}

class Task_readAllFromDB: AsyncTask<Context, Void, Map<Long, String>>(){
    override fun doInBackground(vararg params: Context): Map<Long, String> {
        val context = params[0]
        val db = RecipeDatabase.getRecipeDatabase(context)
        val recipes = db.recipeDao().idAndName
//        val ids = db.recipeDao().iDs
//        val names = db.recipeDao().names
        db.close()
        val recipeMap = mutableMapOf<Long, String>()
        for (i in recipes.indices){ //for (i in ids.indices){
//            recipeMap.put(ids[i], names[i])
            recipeMap.put(recipes[i].id, recipes[i].name)
        }
        return recipeMap
    }
}

class Task_getNameAndAddressById: AsyncTask<Array<Any>, Void, Array<String>>(){
    override fun doInBackground(vararg params: Array<Any>): Array<String> {
        val id = params[0][0] as Long
        val context = params[0][1] as Context
        val db = RecipeDatabase.getRecipeDatabase(context)
        val address = db.recipeDao().getAddressById(id)
        val name = db.recipeDao().getNameById(id)
        db.close()
        return arrayOf(name, address)
    }
}

class Task_getTitle: AsyncTask<String, Void, String>(){
    override fun doInBackground(vararg params: String?): String {
//        val str: String
//        str = try{
//            val doc = Jsoup.connect(params[0]).get()
//            val elements = doc.select("title")
//            val nodes = elements[0].textNodes()
//            nodes[0].toString()
//        } catch (e: Exception){
//            Log.e("[GET REQUEST]", e.localizedMessage)
//            "Exception"
//        }
//        return str

//        val title: String
//        title = try {
//            var str = ""
//            val doc = Jsoup.connect(params[0]).get()
//            val elements = doc.select("meta")
//            elements.forEach {
//                val prop = it.attr("property")
//                if (prop == "og:title"){
//                    str = it.attr("content")
//                }
//            }
//            str
//        } catch (ex: Exception){
//            Log.e("[GET REQUEST]", ex.localizedMessage)
//            "Exception"
//        }
//        return title

        return try {
            val doc = Jsoup.connect(params[0]).get()
            doc.title()
        } catch (ex: Exception){
            Log.e("[GET REQUEST]", ex.localizedMessage)
            ""
        }
    }
}

class Task_getImageUrl:AsyncTask<String, Void, String>(){
    override fun doInBackground(vararg params: String?): String {
        var imageUrl = ""
        try {
            val doc = Jsoup.connect(params[0]).get()
            val elements = doc.select("meta")
            elements.forEach {
                val prop = it.attr("property")
                if (prop == "og:image"){
                    imageUrl = it.attr("content")
                }
            }
        } catch (ex: Exception){
            Log.e("[GET REQUEST]", ex.localizedMessage)
//            "Url is absent"
        }
        return imageUrl
    }

}