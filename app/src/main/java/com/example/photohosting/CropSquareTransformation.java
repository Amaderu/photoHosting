package com.example.photohosting;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

public class CropSquareTransformation implements Transformation {
    ImageView view;
    @Override public Bitmap transform(Bitmap source) {
        Math.min(view.getWidth(), view.getHeight());
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
        if (result != source) {
            //высвобождение Dalvik памяти Bitmap
            source.recycle();
        }
        return result;
    }

    @Override public String key() { return "square()"; }
}
