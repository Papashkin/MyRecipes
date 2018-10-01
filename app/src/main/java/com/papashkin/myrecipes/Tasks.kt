package com.papashkin.myrecipes

import android.content.Context
import android.os.AsyncTask

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
        val ids = db.recipeDao().iDs
        val names = db.recipeDao().names
        db.close()
        val recipeMap = mutableMapOf<Long, String>()
        for (i in ids.indices){
            recipeMap.put(ids[i], names[i])
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