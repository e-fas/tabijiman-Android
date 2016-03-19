package net.e_fas.oss.tabijiman;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

public class SaveShareView extends FragmentActivity {

    static ImageView PreView;
    static ImageButton SaveButton;
    static ImageButton ShareButton;
    static Bitmap PreviewImage;
    static String PictureData;
    static File SavePath;

    public static void e_print(Object object) {
        Log.e("d_tabijiman", object.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        e_print("onResume");

        if (SavePath != null) {
            e_print(SavePath + " >> Deleted");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        e_print("onStop");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saveshareview);

        Intent g = getIntent();
        PictureData = g.getStringExtra("PictureData");
        BufferedInputStream bis = null;

        try {

            bis = new BufferedInputStream(new FileInputStream(PictureData));
            PreviewImage = BitmapFactory.decodeStream(bis);

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } finally {

            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {

                //IOException, NullPointerException
                e.printStackTrace();
            }
        }

        PreView = (ImageView) findViewById(R.id.PreviewView);
        PreView.setImageBitmap(PreviewImage);

        SaveButton = (ImageButton) findViewById(R.id.Savebutton);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBitmap(PreviewImage);
            }
        });

        ShareButton = (ImageButton) findViewById(R.id.ShareButton);
        ShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                e_print("Picture_Path >> " + PictureData);
                saveBitmap(PreviewImage);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); /* 追加 */
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); /* 追加 */
                intent.setType("text/plain").putExtra(Intent.EXTRA_TEXT, AppSetting.shereTag);
                intent.setType("image/png").putExtra(Intent.EXTRA_STREAM, Uri.fromFile(SavePath));
                intent.setDataAndType(Uri.fromFile(SavePath), "image/png");
                startActivity(intent);
            }
        });

    }

    private void registAndroidDB(String path) {

        // アンドロイドのデータベースへ登録
        // (登録しないとギャラリーなどにすぐに反映されないため)
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    public void saveBitmap(Bitmap bmp) {

        String _time = "";
        Calendar cal = Calendar.getInstance();
        int millisecond = cal.get(Calendar.MILLISECOND);
        int second = cal.get(Calendar.SECOND);
        int minute = cal.get(Calendar.MINUTE);
        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
        _time = "image_" + hourofday + "" + minute + "" + second + ""
                + millisecond + ".png";
        String file_path = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/tabijiman";
        try {
            File dir = new File(file_path);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, _time);
            e_print("Save Path >> " + file.getPath());
            SavePath = file;
            FileOutputStream fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            fOut.flush();
            fOut.close();

            registAndroidDB(file.getPath());

            Toast.makeText(getApplicationContext(),
                    "画像をギャラリーに保存しました",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("error in saving image", e.getMessage());
        }
    }
}
