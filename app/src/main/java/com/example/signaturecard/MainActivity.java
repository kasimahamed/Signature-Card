package com.example.signaturecard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.signaturecard.ViewModel.SignatureViewModel;
import com.example.signaturecard.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityMainBinding binding;
    private BottomSheetDialog dialog;
    private int PERMISSION_REQUEST_CODE = 7;
    private OutputStream outputStream;
    private boolean share = false;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;
    int brushColor = 0;
    private SignatureViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);
        viewModel = new ViewModelProvider(this).get(SignatureViewModel.class);


        binding.drawingView.setBrushColor(R.color.black);
        binding.drawingView.setSizeForBrush(5);
        binding.redo.setOnClickListener(this::onClick);
        binding.undo.setOnClickListener(this::onClick);
        binding.brushSize.setOnClickListener(this::onClick);
        binding.cleaner.setOnClickListener(this::onClick);
        binding.colourPicker.setOnClickListener(this::onClick);
        binding.dropdownMenu.setOnClickListener(this::onClick);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.redo) {
            binding.drawingView.redo();
        } else if (view.getId() == R.id.undo) {
            binding.drawingView.undo();
        } else if (view.getId() == R.id.brush_size) {
            if (brushColor == 0){
                binding.drawingView.setBrushColor(R.color.black);
            }else {
                binding.drawingView.setBrushColor(brushColor);
            }            binding.brushSizeLayout.setVisibility(View.VISIBLE);
            seekbarAction();
        } else if (view.getId() == R.id.cleaner) {
            binding.drawingView.erase(Color.WHITE);
        } else if (view.getId() == R.id.colour_picker) {
            brushColorPicker(view);
        } else if (view.getId() == R.id.dropdown_menu) {
            showMenuList();
        } else if (view.getId() == R.id.clearScreen) {
            binding.drawingView.clearDrawingBoard();
            dialog.dismiss();
        } else if (view.getId() == R.id.saveAsImage) {
            try {
                saveAsImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.exportTo) {
            exportTo();
        } else if (view.getId() == R.id.jpg) {
            alertDialog.dismiss();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                createFolder(".jpg");
            } else {
                askPermission();
            }
        } else if (view.getId() == R.id.png) {
            alertDialog.dismiss();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                createFolder(".png");
            } else {
                askPermission();
            }
        } else if (view.getId() == R.id.share) {
            shareImageUri();
        }
    }

    private void exportTo() {
        dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.image_type_layout, null);
        dialogBuilder.setView(dialogView);

        TextView jpg = (TextView) dialogView.findViewById(R.id.jpg);
        TextView png = (TextView) dialogView.findViewById(R.id.png);
        jpg.setOnClickListener(this::onClick);
        png.setOnClickListener(this::onClick);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void saveAsImage() throws IOException {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createFolder(".jpg");
        } else {
            askPermission();
        }
    }

    private void askPermission() {
        viewModel.askPermission(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createFolder(".jpg");
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void createFolder(String type) {
        String folder_main = "Signature";
        File file = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!file.exists()) {
            file.mkdir();
        }

        Bitmap drawing = viewModel.viewToBitmap(binding.drawingView);
        binding.img.setImageBitmap(drawing);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.img.getDrawable();
        Bitmap bitmap1 = bitmapDrawable.getBitmap();

        File file1 = new File(file, System.currentTimeMillis() + type);
        try {
            outputStream = new FileOutputStream(file1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (type.equals(".jpg")) {
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } else if (type.equals(".png")) {
            bitmap1.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        }
        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shareImageUri() {
        Drawable drawable = binding.img.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        try {
            File file = new File(getApplicationContext().getExternalCacheDir(), File.separator + System.currentTimeMillis() + ".png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);

            intent.putExtra(Intent.EXTRA_STREAM, photoURI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/jpg");

            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMenuList() {
        dialog = new BottomSheetDialog(MainActivity.this);
        dialog.setContentView(R.layout.menu_view);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        TextView clearScreen = dialog.findViewById(R.id.clearScreen);
        TextView saveAsImage = dialog.findViewById(R.id.saveAsImage);
        TextView exportTo = dialog.findViewById(R.id.exportTo);
        TextView shareVia = dialog.findViewById(R.id.share);
        clearScreen.setOnClickListener(this);
        saveAsImage.setOnClickListener(this);
        exportTo.setOnClickListener(this);
        shareVia.setOnClickListener(this);
    }

    private void brushColorPicker(View view) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, R.color.black, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                brushColor = color;
                binding.drawingView.setBrushColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }
        });
        dialog.show();

    }

    private void seekbarAction() {
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int brushSize = i;
                binding.drawingView.setSizeForBrush(brushSize);
                binding.brushSizeTv.setText(i + " / 100");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.brushSizeLayout.setVisibility(View.GONE);
            }
        });
    }
}