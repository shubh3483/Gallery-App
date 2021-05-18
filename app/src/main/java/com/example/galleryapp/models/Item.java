package com.example.galleryapp.models;

public class Item {

    public String imageRedirectedUrl;
    public int color;
    public String label;


    public Item(String imageRedirectedUrl, int color, String label) {
        this.imageRedirectedUrl = imageRedirectedUrl;
        this.color = color;
        this.label = label;
    }
}
