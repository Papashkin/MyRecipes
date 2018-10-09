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

//    @Query("SELECT id, name FROM recipesTab")
//    List<Recipe> getIdAndName();

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

    @Query("SELECT imageUrl FROM recipesTab")
    List<String> getImageUrls();

    @Query("SELECT imageUrl FROM recipesTab WHERE id = :id")
    String getImageUrlById(Long id);

    @Query("UPDATE recipesTab SET imageUrl = :url WHERE id = :id")
    void updImageUrl(String url, Long id);

    @Query("UPDATE recipesTab SET text = :text WHERE id = :id")
    void updText(String text, Long id);

    @Query("UPDATE recipesTab SET name = :text WHERE id = :id")
    void updTitle(String text, Long id);

    @Insert
    Long insert(Recipe recipe);

    @Delete
    void delete(Recipe recipe);

}
