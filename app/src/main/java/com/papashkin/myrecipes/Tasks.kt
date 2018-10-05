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
        val id = if (recipe.name.isNotEmpty()){
            db.recipeDao().insert(recipe)
        } else {
            -1L
        }
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

//class Task_readIdsAndNames: AsyncTask<Context, Void, Map<Long, String>>(){
//    override fun doInBackground(vararg params: Context): Map<Long, String> {
//        val context = params[0]
//        val db = RecipeDatabase.getRecipeDatabase(context)
//        val recipes = db.recipeDao().idAndName
//        db.close()
//        val recipeMap = mutableMapOf<Long, String>()
//        recipes.forEach {
//            recipeMap.put(it.id, it.name)
//        }
//        return recipeMap
//    }
//}

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
        }
        return imageUrl
    }
}

//class Task_getImageUrlById: AsyncTask<Array<Any>, Void, String>(){
//    override fun doInBackground(vararg params: Array<Any>): String {
//        val id = params[0][0] as Long
//        val context = params[0][1] as Context
//        val db = RecipeDatabase.getRecipeDatabase(context)
//        val imgUrl = db.recipeDao().getImageUrlById(id)
//        db.close()
//        return imgUrl
//    }
//}

class Task_getAddressById: AsyncTask<Array<Any>, Void, String>(){
    override fun doInBackground(vararg params: Array<Any>): String {
        val id = params[0][0] as Long
        val context = params[0][1] as Context
        val db = RecipeDatabase.getRecipeDatabase(context)
        val address = db.recipeDao().getAddressById(id)
        db.close()
        return address
    }
}

class Task_checkEmptyRecords: AsyncTask<Context, Void, Boolean>(){
    override fun doInBackground(vararg params: Context): Boolean {
        val isReady = false
        val context = params[0]
        val db = RecipeDatabase.getRecipeDatabase(context)
        val recipes = db.recipeDao().all
        for (i in recipes.indices){
            val name = recipes[i].name
            val address = recipes[i].address
            if (name.isEmpty() || address.isEmpty()){
                db.recipeDao().delete(recipes[i])
            }
        }
        db.close()
        return !isReady
    }
}