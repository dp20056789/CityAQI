package com.forsta.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapFormat {
	public static Bitmap format(Bitmap bitmap){
		if(bitmap == null)
			return null ;
		
		int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        int newWidth = 540;  
        int newHeight = 800; 

        float scaleWidth = ((float) newWidth) / width;  
        float scaleHeight = ((float) newHeight) / height;  
        Matrix matrix = new Matrix();  
        matrix.postScale(scaleWidth, scaleHeight);  
		return Bitmap.createBitmap(bitmap, 0, 0, width,  
                height, matrix, true);
	}
}
