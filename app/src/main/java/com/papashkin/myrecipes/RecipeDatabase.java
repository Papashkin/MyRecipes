package com.papashkin.myrecipes;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Recipe.class}, version = 2)
public abstract class RecipeDatabase extends RoomDatabase {
    private static RecipeDatabase INSTANCE;
    public abstract RecipeDao recipeDao();

    public static RecipeDatabase getRecipeDatabase(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, RecipeDatabase.class, "recipeDB").build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
