package com.example.galleryapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.galleryapp.databinding.ActivityMainBinding;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Objects;

public class ContextMenuHandler {

    ActivityMainBinding b;
    private ImageView iv;
    private String sharePath="no";
    Context context;

    public ContextMenuHandler(Context context) {
        this.context = context;
    }

    public void takeScreenshot(int position) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {

            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpeg";

            //View v2 = findViewById(R.id.parentCard);
            System.out.println("POSITION IS " + position);
            View v1 = Objects.requireNonNull(b.list.findViewHolderForAdapterPosition(position)).itemView;
            v1.setDrawingCacheEnabled(false);
            MaterialCardView materialCardView = v1.findViewById(R.id.parentCard);
            int totalHeight = materialCardView.getChildAt(0).getHeight();
            int totalWidth = materialCardView.getChildAt(0).getWidth();
            v1.layout(0, 0, totalWidth, totalHeight);
            v1.buildDrawingCache(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //setting screenshot in imageview
            String filePath = imageFile.getPath();

            Bitmap ssbitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            iv.setImageBitmap(ssbitmap);
            sharePath = filePath;
            share(sharePath);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void share(String sharePath){

        if(!sharePath.equals("no")) {
            Log.d("ffff", sharePath);
            File file = new File(sharePath);
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(intent);
        }else{
            System.out.println("NOT WORKING");
        }
    }

}
