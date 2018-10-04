package com.papashkin.myrecipes;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "recipesTab")
class Recipe {

    @PrimaryKey(autoGenerate = true)
    Long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "imageUrl")
    private String imageUrl;

    Recipe(String name, String address, String text){
        this.name = name;
        this.address = address;
        this.text = text;
        this.imageUrl = "";
    }

    Recipe(String name, String address, String text, String imgUrl){
        this.name = name;
        this.address = address;
        this.text = text;
        this.imageUrl = imgUrl;
    }

    Recipe(String name, String imgUrl){
        this.name = name;
        this.address = "";
        this.text = "";
        this.imageUrl = imgUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long uid) {
        this.id = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
