package com.example.signaturecard.ViewModel;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModel;

public class SignatureViewModel extends ViewModel {
    private int PERMISSION_REQUEST_CODE = 7;

    public void askPermission(Context context){
        ActivityCompat.requestPermissions((Activity) context, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, PERMISSION_REQUEST_CODE);
    }


    public Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

}
