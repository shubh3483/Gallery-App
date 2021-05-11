package com.example.galleryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHelper {

    private Context context;
    private OnCompleteListener listener;
    String rectangleImageURL = "https://picsum.photos/%d/%d";
    String squareImageURL = "https://picsum.photos/%d";
    private Bitmap bitmap;
    private Set<Integer> colors;

    void fetchData(int x, int y, Context context, OnCompleteListener listener){
        this.context = context;

        this.listener = listener;
        fetchImage(String.format(rectangleImageURL, x, y));
    }

    void fetchData(int x, Context context, OnCompleteListener listener){
        this.context = context;

        this.listener = listener;
        fetchImage(String.format(squareImageURL, x));
    }

    void fetchImage(String url){
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void extractPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                colors = getColoursFromPalette(p);
                labelImage();
            }
        });
    }

    private void labelImage() {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        List<String> strings = new ArrayList<>();
                        for(ImageLabel label : labels){
                            strings.add(label.getText());
                        }
                        listener.onFetched(bitmap,colors,strings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.setError(e.toString());
                    }
                });
    }

    private Set<Integer> getColoursFromPalette(Palette p) {
        Set<Integer> colors = new HashSet<>();

        colors.add(p.getVibrantColor(0));
        colors.add(p.getDarkVibrantColor(0));
        colors.add(p.getLightVibrantColor(0));

        colors.add(p.getMutedColor(0));
        colors.add(p.getDarkMutedColor(0));
        colors.add(p.getLightMutedColor(0));

        colors.add(p.getVibrantColor(0));

        colors.remove(0);

        return colors;
    }

    interface OnCompleteListener{
        void onFetched(Bitmap bitmap, Set<Integer> colors, List<String> labels);
        void setError(String error);
    }
}
