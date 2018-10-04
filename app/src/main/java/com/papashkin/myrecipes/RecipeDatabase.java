package com.papashkin.myrecipes;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {Recipe.class}, version = 3)
public abstract class RecipeDatabase extends RoomDatabase {
    private static RecipeDatabase INSTANCE;
    public abstract RecipeDao recipeDao();

    public static RecipeDatabase getRecipeDatabase(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, RecipeDatabase.class, "recipeDB")
                    .addMigrations(MIGRATION_2_3)
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    static final Migration MIGRATION_2_3 = new Migration(2,3){
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE recipesTab ADD COLUMN imageUrl TEXT");
        }
    };
}
