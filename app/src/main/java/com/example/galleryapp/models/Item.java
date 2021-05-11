package com.example.galleryapp.models;

import android.accessibilityservice.GestureDescription;
import android.graphics.Bitmap;

public class Item {

    public Bitmap bitmap;
    public int color;
    public String label;

    public Item(Bitmap bitmap, int color, String label) {
        this.bitmap = bitmap;
        this.color = color;
        this.label = label;
    }
}
