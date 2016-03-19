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

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TakePicture extends Activity implements SurfaceHolder.Callback {

    //サーフェイスをコントロールするholderオブジェクト
    static SurfaceHolder holder;
    //カメラオブジェクトの取得
    static Camera camera;
    static ImageButton shutterButton;
    static ImageButton selectFrame;

    static HorizontalScrollView FrameSelectView;
    static LinearLayout ScrollViewLinear;
    static LinearLayout FrameViewLinear;
    static RelativeLayout FrameViewRelative;
    static ImageButton FramePreview;
    static ImageView FrameView;
    static SurfaceView PreviewView;
    static SQLiteHelper helper;
    static SQLiteDatabase db;

    public static void e_print(Object object) {
        Log.e("d_tabiziman", object.toString());
    }

    //YUVをBitmapに変換する関数
    public static Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {

        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
    }

    // Frame_Initializer
    public void Frame_initialize() {

        FrameView = (ImageView) findViewById(R.id.FrameView);
        FrameViewLinear = (LinearLayout) findViewById(R.id.FrameViewLinear);
        FrameViewRelative = (RelativeLayout) findViewById(R.id.FrameViewRelative);

        FrameSelectView = (HorizontalScrollView) findViewById(R.id.FrameSelectView);
        ScrollViewLinear = (LinearLayout) findViewById(R.id.ScrollViewLinear);
        PreviewView = (SurfaceView) findViewById(R.id.PreviewView);

        ScrollViewLinear.removeAllViews();

        helper = new SQLiteHelper(this);
        db = helper.getWritableDatabase();

        List<String> NameList = AppSetting.GetFileNames();

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.MATCH_PARENT);

        for (int i = 0; i < AppSetting.initFrameName.length; i++) {

            FrameViewLinear = new LinearLayout(this);
            FrameViewLinear.setBackground(null);
            FramePreview = new ImageButton(this);

            try {

                InputStream is = getResources().getAssets().open(AppSetting.initFrameName[i]);
                final Bitmap bm = BitmapFactory.decodeStream(is);
                FramePreview.setImageBitmap(bm);
                FramePreview.setBackground(null);
                FramePreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                FramePreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shutterButton.setActivated(true);
                        FrameView.setImageBitmap(bm);
                        FrameView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        FrameView.setAlpha(0.8f);
                    }
                });
            } catch (IOException e) { /* 例外処理 */

                e.printStackTrace();
            }

            FrameViewLinear.addView(FramePreview, params);
            ScrollViewLinear.addView(FrameViewLinear);
        }

        for (String name : NameList) {

            e_print("name >> " + name);

            FrameViewLinear = new LinearLayout(this);
            FrameViewLinear.setBackground(null);
            FramePreview = new ImageButton(this);

            try {

                final Bitmap temp = AppSetting.loadBitmapSDCard(name);
                FramePreview.setImageBitmap(temp);
                FramePreview.setBackground(null);
                FramePreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                FramePreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shutterButton.setActivated(true);
                        FrameView.setImageBitmap(temp);
                        FrameView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        FrameView.setAlpha(0.8f);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            FrameViewLinear.addView(FramePreview, params);
            ScrollViewLinear.addView(FrameViewLinear);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_picture);

        Frame_initialize();

        shutterButton = (ImageButton) findViewById(R.id.Shutter);
        shutterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (v.isActivated()) {

                    e_print("shutter >> enable");

                    camera.autoFocus(new Camera.AutoFocusCallback() {

                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {


                            camera.setOneShotPreviewCallback(new Camera.PreviewCallback() {

                                @Override
                                public void onPreviewFrame(byte[] data, Camera camera) {

                                    //プレビューのフォーマットはYUVなので、YUVをBmpに変換する
                                    int w = camera.getParameters().getPreviewSize().width;
                                    int h = camera.getParameters().getPreviewSize().height;

                                    //切り取った画像を保存
                                    Bitmap bmp = getBitmapImageFromYUV(data, w, h);

                                    //回転
                                    Matrix m = new Matrix();
                                    m.setRotate(90);

                                    //保存用 bitmap生成
                                    Bitmap rotated_bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                                    Bitmap result_bmp = captureScreen(rotated_bmp);

                                    String imageName = "temp.png";

                                    File imageFile = new File(getFilesDir(), imageName);
                                    FileOutputStream out;

                                    try {
                                        out = new FileOutputStream(imageFile);
                                        result_bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                        ///画像をアプリの内部領域に保存
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }

                                    Intent SaveShareView = new Intent(getApplicationContext(), SaveShareView.class);
                                    SaveShareView.putExtra("PictureData", imageFile.getAbsolutePath());
                                    startActivity(SaveShareView);
                                }
                            });
                        }
                    });

                } else {
                    e_print("shutter >> disable");
                }
            }
        });

        selectFrame = (ImageButton) findViewById(R.id.SelectFrame);
        selectFrame.setActivated(true);
        selectFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isActivated()) {
                    FrameSelectView.setVisibility(View.INVISIBLE);
                    v.setActivated(false);
                } else {
                    FrameSelectView.setVisibility(View.VISIBLE);
                    v.setActivated(true);
                }
            }
        });

        //SurfaceViewからholderオブジェクトを取得
        SurfaceView s = (SurfaceView) findViewById(R.id.PreviewView);

        s.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                camera.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                    }
                });
            }
        });

        holder = s.getHolder();
        holder.addCallback(this);


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //カメラリソースの取得
        camera = Camera.open();

        camera.setDisplayOrientation(90);

        if (camera != null) {
            try {
                //SurfaceViewをプレビューディスプレイとして設定
                camera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        params.setPreviewSize(sizes.get(0).width, sizes.get(0).height);

        camera.setParameters(params);

        if (camera != null) {
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (camera != null) {
            //プレビューの停止
            camera.stopPreview();
            //カメラ解放
            camera.release();
            camera = null;
        }
    }

    private Bitmap captureScreen(Bitmap bitmap) {

        // フォトフレーム部のキャプチャ取得
        FrameView.setDrawingCacheEnabled(false);
        FrameView.setDrawingCacheEnabled(true);

        Bitmap Frame = ((BitmapDrawable) FrameView.getDrawable()).getBitmap();

        Rect previewview = new Rect();
        PreviewView.getDrawingRect(previewview);

        Rect frameview = new Rect();
        FrameView.getDrawingRect(frameview);

        Rect tabbar = new Rect();
        findViewById(R.id.Tabbar).getDrawingRect(tabbar);

        // カメラとフレーム（オーバレイ）を重ねる
        Bitmap temp = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight() - tabbar.height(),
                Bitmap.Config.ARGB_8888);

        // "offBitmap"が２つを張り合わせたBitmap（キャプチャ画像）となります。
        Canvas C_temp = new Canvas(temp);

        Rect temp_Rect = new Rect(
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight() - tabbar.height()
        );

        C_temp.drawBitmap(bitmap, temp_Rect, temp_Rect, null);

        bitmap = temp;

        new PictureUtil();

        // カメラとフレーム（オーバレイ）を重ねる
        Bitmap offBitmap = Bitmap.createBitmap(
                Frame.getWidth(),
                Frame.getHeight(),
                Bitmap.Config.ARGB_8888);

        // "offBitmap"が２つを張り合わせたBitmap（キャプチャ画像）となります。
        Canvas offScreen = new Canvas(offBitmap);

        float scale;

        float[] f_Matrix = new float[9];
        TakePicture.FrameView.getImageMatrix().getValues(f_Matrix);
        Matrix matrix = TakePicture.FrameView.getImageMatrix();

        if (f_Matrix[Matrix.MTRANS_Y] == 0) {

            scale = PictureUtil.getInitialScale(bitmap, offScreen)[1];
            e_print("scale based on Height");
        } else {
            scale = PictureUtil.getInitialScale(bitmap, offScreen)[0];
            e_print("scale based on Width");
        }

        matrix.postScale(scale, scale);
        TakePicture.FrameView.setImageMatrix(matrix);
        TakePicture.FrameView.getImageMatrix().getValues(f_Matrix);

        e_print("Frame_Bitmap >> width:: " + Frame.getWidth() + " height:: " + Frame.getHeight());
        e_print("bitmap_Size >> width:: " + bitmap.getWidth() + " height:: " + bitmap.getHeight());
        e_print("scale >> " + scale);
        e_print("matrix_X >> " + f_Matrix[Matrix.MTRANS_X] + " matrix_Y >> " + f_Matrix[Matrix.MTRANS_Y]);
        e_print("matrix_X_scale >> " + f_Matrix[Matrix.MSCALE_X] + " matrix_Y_scale >> " + f_Matrix[Matrix.MSCALE_Y]);
        e_print("matrix_X / scale >> " + f_Matrix[Matrix.MTRANS_X] / scale + " matrix_Y / scale >> " + f_Matrix[Matrix.MTRANS_Y] / scale);

        float zureX = f_Matrix[Matrix.MTRANS_X] / f_Matrix[Matrix.MSCALE_X];
        float zureY = f_Matrix[Matrix.MTRANS_Y] / f_Matrix[Matrix.MSCALE_Y];
        e_print("zureX >> " + zureX + " zureY >> " + zureY);
        float frameX = zureX * scale;
        float frameY = zureY * scale;
        e_print("frameX >> " + frameX + " frameY >> " + frameY);

        Rect srcRect = new Rect(
                (int) frameX,
                (int) frameY,
                bitmap.getWidth() - (int) frameX,
                bitmap.getHeight() - (int) frameY
        );

        Rect dstRect = new Rect(
                0,
                0,
                Frame.getWidth(),
                Frame.getHeight()
        );

        e_print("srcRect >> " + srcRect + "  dstRect >> " + dstRect);

        offScreen.drawBitmap(bitmap, srcRect, dstRect, null);

        offScreen.drawBitmap(Frame, dstRect, dstRect, null);

        return offBitmap;
    }
}