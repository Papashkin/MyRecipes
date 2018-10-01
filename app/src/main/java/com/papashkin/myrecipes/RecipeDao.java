package com.papashkin.myrecipes;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipesTab")
    List<Recipe> getAll();

    @Query("SELECT id, name FROM recipesTab")
    List<Recipe> getIdAndName();

    @Query("SELECT id FROM recipesTab")
    List<Long> getIDs();

    @Query("SELECT name FROM recipesTab")
    List<String> getNames();

    @Query("SELECT address FROM recipesTab")
    List<String> getAddresses();

    @Query("SELECT * FROM recipesTab WHERE id = :recipeId")
    List<Recipe> getAllById(Long recipeId);

    @Query("Select * from recipesTab where address = :recipeAddress")
    List<Recipe> getAllByAddress(String recipeAddress);

    @Query("SELECT address FROM recipesTab WHERE id = :recipeId")
    String getAddressById(Long recipeId);

    @Query("SELECT name FROM recipesTab WHERE id = :recipeId")
    String getNameById(Long recipeId);

    @Query("SELECT id FROM recipesTab WHERE address = :recipeAddress")
    Long getIdByAddress(String recipeAddress);

    @Insert
    Long insert(Recipe recipe);

    @Delete
    void delete(Recipe recipe);

}
