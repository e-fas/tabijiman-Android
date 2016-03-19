//  Copyright (c) 2016 FUKUI Association of information & system industry. All rights reserved.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.

package net.e_fas.oss.tabijiman;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

final class PictureUtil extends FragmentActivity {


    private static void e_print(Object object) {
        Log.e("d_tabiziman", object.toString());
    }

    public static float[] getInitialScale(Bitmap bitmap, Canvas view) {

        // Viewのサイズ
        float FrameWidth = view.getWidth();
        float FrameHeight = view.getHeight();

        // bitmapのサイズ
        float imageWidth = bitmap.getWidth();
        float imageHeight = bitmap.getHeight();

        // X軸、Y軸のサイズ比
        float scaleX = imageWidth / FrameWidth;
        float scaleY = imageHeight / FrameHeight;

        e_print("X >> " + scaleX + " Y >> " + scaleY);

        // 初期状態で画像の見切れをなくしたいので、
        // 小さいほうに合わせる
//        return Math.min(scaleX, scaleY);
        return new float[]{scaleX, scaleY};
    }
}
