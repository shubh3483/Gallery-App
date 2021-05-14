package com.example.galleryapp.models;

import android.accessibilityservice.GestureDescription;
import android.graphics.Bitmap;

public class Item {

    public String bitmapAsString;
    public int color;
    public String label;


    public Item(String bitmapAsString, int color, String label) {
        this.bitmapAsString = bitmapAsString;
        this.color = color;
        this.label = label;
    }
}
