package com.example.galleryapp;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.galleryapp.databinding.ActivityDemoBinding;
import com.example.galleryapp.databinding.ChipColourBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;

import java.util.List;
import java.util.Set;

public class DemoActivity extends AppCompatActivity {

    ActivityDemoBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        new ItemHelper()
                .fetchData(1920, 1080, getApplicationContext(), new ItemHelper.OnCompleteListener() {
                    @Override
                    public void onFetched(Bitmap bitmap, Set<Integer> colors, List<String> labels) {
                        b.imageView.setImageBitmap(bitmap);
                        inflateColourChips(colors);
                        inflateLabelChips(labels);
                    }

                    @Override
                    public void setError(String error) {

                    }
                });
    }



    private void inflateLabelChips(List<String> labels) {
        for(String label : labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(getLayoutInflater());
            binding.getRoot().setText(label);
            b.labelChipGrp.addView(binding.getRoot());
        }
    }



    private void inflateColourChips(Set<Integer> colors) {
        for(int colour : colors){
            ChipColourBinding binding = ChipColourBinding.inflate(getLayoutInflater());
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(colour));
            b.colourPaletteChipGrp.addView(binding.getRoot());
        }
    }



    /*private void testDialog() {

        DialogAddImageBinding binding = DialogAddImageBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new MaterialAlertDialogBuilder(this,R.style.CustomDialogTheme)
                .setView(binding.getRoot())
                .show();

        binding.fetchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.enterDimensionsRoot.setVisibility(View.GONE);
                binding.progressIndicatorRoot.setVisibility(View.VISIBLE);

                new Handler()
                        .postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.progressIndicatorRoot.setVisibility(View.GONE);
                                binding.mainRoot.setVisibility(View.VISIBLE);
                            }
                        },5000);
            }
        });
        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }*/
}