package com.example.galleryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHelper{

    private Context context;
    private OnCompleteListener listener;
    String rectangleImageURL = "https://picsum.photos/%d/%d";
    String squareImageURL = "https://picsum.photos/%d";
    private Bitmap bitmap;
    private Set<Integer> colors;
    String globalFinalUrl = null;
    Uri uri = null;


    /**
     * The below two "fetchData" methods will create particular URL and it will call fetchImage
     * methods.
     * @param x
     * @param y
     * @param context
     * @param listener
     * @throws IOException
     */
    void fetchData(int x, int y, Context context, OnCompleteListener listener) throws IOException {
        this.context = context;
        this.listener = listener;
        fetchImage(String.format(rectangleImageURL, x, y), context, listener);
    }

    void fetchData(int x, Context context, OnCompleteListener listener) throws IOException {
        this.context = context;
        this.listener = listener;
        fetchImage(String.format(squareImageURL, x), context, listener);
    }

    /**
     * This method will fetch image according to the URL passed into it.
     * @param url
     */
    void fetchImage(String url, Context context, OnCompleteListener listener) {
        if(this.context == null && this.listener == null){
            this.context = context;
            this.listener = listener;
        }
        new RedirectedUrlHelper().getRedirectedUrl(new RedirectedUrlHelper.OnCompleteListener() {
            @Override
            public void onFetched(String fetchedUrl) {
                globalFinalUrl = fetchedUrl;
                Glide.with(context)
                        .asBitmap()
                        .load(globalFinalUrl)
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
        }).execute(url);
    }

    /**
     * This method will fetch the image with the uri passed in it and will fetch the color and labels
     * according to the added image.
     * @param uri
     * @param context
     * @param listener
     */
    public void fetchGalleryImage(Uri uri, Context context, OnCompleteListener listener){
        this.uri = uri;
        this.listener = listener;
        Glide.with(context)
                .asBitmap()
                .load(uri)
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

    /**
     * This method will extract the color palette from the bitmap.
     */
    private void extractPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                colors = getColoursFromPalette(p);
                labelImage();
            }
        });
    }

    /**
     * This method will extract labels from the bitmap
     */
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
                        if(uri != null){
                            System.out.println(uri);
                            listener.onFetched(uri,colors,strings);
                        }

                        else listener.onFetched(globalFinalUrl,colors,strings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.setError(e.toString());
                    }
                });
    }


    /**
     * This method will fetch all the colors from the bitmap
     * @param p
     * @return
     */
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
        void onFetched(Uri uri, Set<Integer> colors, List<String> labels);
        void onFetched(String redirectedUrl, Set<Integer> colors, List<String> labels);
        void setError(String error);
    }
}
