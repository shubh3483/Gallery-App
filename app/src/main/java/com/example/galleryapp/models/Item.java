package com.example.galleryapp.models;

import android.net.Uri;

public class Item {

    public String imageRedirectedUrl;
    public String uri;
    public int color;
    public String label;


    public Item(String imageRedirectedUrl,String uri, int color, String label) {
        this.uri = uri;
        this.imageRedirectedUrl = imageRedirectedUrl;
        this.color = color;
        this.label = label;
    }
}
