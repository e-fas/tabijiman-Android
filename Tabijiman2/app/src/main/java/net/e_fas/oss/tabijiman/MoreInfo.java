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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.wasabeef.blurry.Blurry;

public class MoreInfo extends AppCompatActivity {

    public static Double Distance;
    static Integer id;
    static String cat;
    static Intent g;
    static SQLiteHelper helper;
    static SQLiteDatabase db;
    static TextView AddressLabel;
    static TextView TitleLabel;
    static ImageView FramePinView;
    static LinearLayout GoToButtonView;
    static LinearLayout TakePictureButtonView;
    static LinearLayout HeaderView;
    static ImageButton GoToButton;
    static ImageButton TakePictureButton;
    static ImageView PreviewImage;
    static TextView DescriptionLabel;
    static ImageButton MoreInfoButton;
    static ImageButton FrameGetButton_Disable;
    static ImageButton FrameGetButton_Enable;
    static ImageView PlacePinView;
    static ImageView CollectionPinView;

    static Bitmap SaveImageBitmap;

    static Double lat;
    static Double lng;
    static String img;
    static Integer area;
    static String imageName;

    public static void e_print(Object object) {
        Log.e("d_tabiziman", object.toString());
    }

    public void print(Object object) {
        Log.d("d_tabiziman", object.toString());
    }

    private View getActionBarView() {

        // 表示するlayoutファイルの取得
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.titlebar, null);

        // CustomViewにクリックイベントを登録する
        ImageButton info = (ImageButton) view.findViewById(R.id.imageButton);
        info.setVisibility(View.GONE);

        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_info);

        // ActionBarの設定
        if (savedInstanceState == null) {

            // customActionBarの取得
            View customActionBarView = this.getActionBarView();
            // ActionBarの取得
            ActionBar actionBar = this.getSupportActionBar();

            if (actionBar != null) {
                // タイトルを表示するか（もちろん表示しない）
                actionBar.setDisplayShowTitleEnabled(false);
                // iconを表示するか（もちろん表示しない）
                actionBar.setDisplayShowHomeEnabled(false);
                // ActionBarにcustomViewを設定する
                actionBar.setCustomView(customActionBarView);
                // CutomViewを表示するか
                actionBar.setDisplayShowCustomEnabled(true);
            }
        }

        HeaderView = (LinearLayout) findViewById(R.id.HeaderView);
        AddressLabel = (TextView) findViewById(R.id.AddressLabel);
        TitleLabel = (TextView) findViewById(R.id.TitleLabel);
        FramePinView = (ImageView) findViewById(R.id.FramePinView);
        PlacePinView = (ImageView) findViewById(R.id.PlacePinView);
        CollectionPinView = (ImageView) findViewById(R.id.CollectionPinView);

        GoToButtonView = (LinearLayout) findViewById(R.id.GoToButtonView);
        GoToButton = (ImageButton) findViewById(R.id.GoToButton);
        TakePictureButtonView = (LinearLayout) findViewById(R.id.TakePictureButtonView);
        TakePictureButton = (ImageButton) findViewById(R.id.TakePictureButton);

        PreviewImage = (ImageView) findViewById(R.id.PreviewImage);
        DescriptionLabel = (TextView) findViewById(R.id.DescriptionLabel);

        MoreInfoButton = (ImageButton) findViewById(R.id.MoreInfoButton);
        FrameGetButton_Disable = (ImageButton) findViewById(R.id.FrameGetButton_Disable);
        FrameGetButton_Enable = (ImageButton) findViewById(R.id.FrameGetButton_Enable);

        TakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent TakePicture = new Intent(getApplicationContext(), TakePicture.class);
                startActivity(TakePicture);
            }
        });

        MoreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.google.co.jp/search?q=" + TitleLabel.getText());
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        });

        g = getIntent();
        id = g.getIntExtra("id", 0);
        cat = g.getStringExtra("cat");
        Distance = g.getDoubleExtra("dist", 1000);
        imageName = g.getStringExtra("imageName");

        helper = new SQLiteHelper(this);
        db = helper.getWritableDatabase();
        SetData(cat);
    }

    public void SetData(String cat) {

        switch (cat) {
            case "place": {

                HeaderView.setBackgroundColor(Color.parseColor("#edf5d3"));
                FramePinView.setVisibility(View.GONE);
                CollectionPinView.setVisibility(View.GONE);
                PlacePinView.setVisibility(View.VISIBLE);
                MoreInfoButton.setVisibility(View.VISIBLE);
                TakePictureButtonView.setVisibility(View.GONE);

                //SQL文
                String select_sql = "SELECT * FROM place WHERE `_id` = ?";
                print("select_sql >> " + select_sql);

                //SQL文の実行
                final Cursor cursor = db.rawQuery(select_sql, new String[]{String.valueOf(id)});
                cursor.moveToFirst();

                try {
                    e_print("cursor_name >> " + cursor.getString(cursor.getColumnIndex("name")));

                    AddressLabel.setText(cursor.getString(cursor.getColumnIndex("address")));
                    TitleLabel.setText(cursor.getString(cursor.getColumnIndex("name")));
                    DescriptionLabel.setText(cursor.getString(cursor.getColumnIndex("desc")));
                    lat = cursor.getDouble(cursor.getColumnIndex("lat"));
                    lng = cursor.getDouble(cursor.getColumnIndex("lng"));

                    GoToButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("geo:"
                                    + lat + "," + lng
                                    + "?q=" + TitleLabel.getText());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });

                    if (!cursor.isNull(cursor.getColumnIndex("img"))) {
                        new DownloadImage(this).execute(cursor.getString(cursor.getColumnIndex("img")), "1");
                    } else {
                        PreviewImage.setImageResource(R.drawable.flame_collection_info_ing_flame_l);
                    }

                } finally {
                    cursor.close();
                }
                break;
            }
            case "frame": {

                HeaderView.setBackgroundColor(Color.parseColor("#ffecca"));
                PlacePinView.setVisibility(View.GONE);
                CollectionPinView.setVisibility(View.GONE);
                FramePinView.setVisibility(View.VISIBLE);

                //SQL文
                final String select_sql = "SELECT * FROM frame WHERE `_id` = ?";
                print("select_sql >> " + select_sql);

                //SQL文の実行
                final Cursor cursor = db.rawQuery(select_sql, new String[]{String.valueOf(id)});
                cursor.moveToFirst();

                try {
                    e_print("cursor_name >> " + cursor.getString(cursor.getColumnIndex("name")));

                    TitleLabel.setText(cursor.getString(cursor.getColumnIndex("name")));
                    DescriptionLabel.setText(cursor.getString(cursor.getColumnIndex("desc")));

                    lat = cursor.getDouble(cursor.getColumnIndex("lat"));
                    lng = cursor.getDouble(cursor.getColumnIndex("lng"));
                    img = cursor.getString(cursor.getColumnIndex("img"));
                    area = cursor.getInt(cursor.getColumnIndex("area"));

                    if (cursor.getInt(cursor.getColumnIndex("getFlag")) == 0) {

                        TakePictureButton.setEnabled(false);
                    } else {

                        TakePictureButton.setEnabled(true);
                    }

                    GoToButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("geo:"
                                    + lat + "," + lng
                                    + "?q=" + TitleLabel.getText());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });

                    e_print("cursor_area >> " + cursor.getInt(cursor.getColumnIndex("area")));

                    if (Distance < cursor.getInt(cursor.getColumnIndex("area"))) {

                        FrameGetButton_Enable.setVisibility(View.VISIBLE);

                        FrameGetButton_Enable.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                try {

                                    AppSetting.savePngSDCard(TitleLabel.getText().toString() + ".png", SaveImageBitmap);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                //SQL文
                                String INSERT_SQL = "REPLACE INTO getFrame( name, desc, lat, lng, img, area, getFlag ) VALUES("
                                        + DatabaseUtils.sqlEscapeString(TitleLabel.getText().toString()) + ","
                                        + DatabaseUtils.sqlEscapeString(DescriptionLabel.getText().toString()) + ","
                                        + DatabaseUtils.sqlEscapeString(String.valueOf(lat)) + ","
                                        + DatabaseUtils.sqlEscapeString(String.valueOf(lng)) + ","
                                        + DatabaseUtils.sqlEscapeString(img) + ","
                                        + DatabaseUtils.sqlEscapeString(String.valueOf(area)) + ","
                                        + DatabaseUtils.sqlEscapeString(String.valueOf(1))
                                        + ")";

                                print("select_sql >> " + INSERT_SQL);

                                db.execSQL(INSERT_SQL);

                                String UPDATE_SQL = "UPDATE frame SET `getFlag` = "
                                        + DatabaseUtils.sqlEscapeString(String.valueOf(1))
                                        + " WHERE `_id` = "
                                        + DatabaseUtils.sqlEscapeString(String.valueOf(id)) + ";";

                                e_print("update_sql >> " + UPDATE_SQL);

                                db.execSQL(UPDATE_SQL);
                            }
                        });
                    } else {

                        FrameGetButton_Disable.setVisibility(View.VISIBLE);
                    }

                    new DownloadImage(this).execute(img, "-1");
                    new DownloadImage(this).execute(img, cursor.getString(cursor.getColumnIndex("getFlag")));

                } finally {
                    cursor.close();
                }
                break;
            }
            case "getFrame": {

                HeaderView.setBackgroundColor(Color.parseColor("#ffecca"));
                PlacePinView.setVisibility(View.GONE);
                FramePinView.setVisibility(View.GONE);
                CollectionPinView.setVisibility(View.VISIBLE);
                MoreInfoButton.setVisibility(View.VISIBLE);

                //SQL文
                String select_sql = "SELECT * FROM getFrame WHERE `_id` = ?";
                print("select_sql >> " + select_sql);

                //SQL文の実行
                Cursor cursor = db.rawQuery(select_sql, new String[]{String.valueOf(id)});
                cursor.moveToFirst();

                try {
                    e_print("cursor_name >> " + cursor.getString(cursor.getColumnIndex("name")));

                    TitleLabel.setText(cursor.getString(cursor.getColumnIndex("name")));
                    DescriptionLabel.setText(cursor.getString(cursor.getColumnIndex("desc")));
                    lat = cursor.getDouble(cursor.getColumnIndex("lat"));
                    lng = cursor.getDouble(cursor.getColumnIndex("lng"));

                    GoToButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("geo:"
                                    + lat + "," + lng
                                    + "?q=" + TitleLabel.getText());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });

                } finally {
                    cursor.close();
                }

//                new AppSetting(this);
                try {
                    final Bitmap temp = AppSetting.loadBitmapSDCard(imageName);
                    MoreInfo.PreviewImage.setImageBitmap(temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
            default: {

                HeaderView.setBackgroundColor(Color.parseColor("#ffecca"));
                PlacePinView.setVisibility(View.GONE);
                FramePinView.setVisibility(View.GONE);
                CollectionPinView.setVisibility(View.VISIBLE);
                MoreInfoButton.setVisibility(View.VISIBLE);

                //SQL文
                String select_sql = "SELECT * FROM init WHERE `_id` = ?";
                print("select_sql >> " + select_sql);

                //SQL文の実行
                Cursor cursor = db.rawQuery(select_sql, new String[]{String.valueOf(id)});
                cursor.moveToFirst();

                try {
                    e_print("cursor_name >> " + cursor.getString(cursor.getColumnIndex("name")));

                    TitleLabel.setText(cursor.getString(cursor.getColumnIndex("name")));
                    DescriptionLabel.setText(cursor.getString(cursor.getColumnIndex("desc")));
                    lat = cursor.getDouble(cursor.getColumnIndex("lat"));
                    lng = cursor.getDouble(cursor.getColumnIndex("lng"));

                    GoToButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("geo:"
                                    + lat + "," + lng
                                    + "?q=" + TitleLabel.getText());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });

                } finally {
                    cursor.close();
                }

                InputStream is = null;
                try {
                    is = getResources().getAssets().open(imageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final Bitmap bm = BitmapFactory.decodeStream(is);
                MoreInfo.PreviewImage.setImageBitmap(bm);

                break;
            }
        }


    }
}

class DownloadImage extends AsyncTask<String, Void, Bitmap> {

    static String RenderFlag;
    static Context context;

    public DownloadImage(Context c) {
        context = c;
    }

    public void print(Object object) {
        Log.d("d_tabiziman", object.toString());
    }

    public void e_print(Object object) {
        Log.e("d_tabiziman", object.toString());
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        switch (RenderFlag) {

            case "-1":
                MoreInfo.SaveImageBitmap = result;
                break;

            case "0":
                try {

                    MoreInfo.PreviewImage.setImageBitmap(result);

                    Blurry.with(context)
                            .radius(15)
                            .sampling(8)
                            .capture(MoreInfo.PreviewImage)
                            .into(MoreInfo.PreviewImage);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "1":
                MoreInfo.PreviewImage.setImageBitmap(result);
                break;
        }
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        RenderFlag = params[1];
        e_print("download_url >> " + params[0]);

        Bitmap bmp = null;

        try {
            URL url = new URL(params[0]);

            // HttpURLConnection インスタンス生成
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // タイムアウト設定
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(20000);

            // リクエストメソッド
            urlConnection.setRequestMethod("GET");

            // リダイレクトを自動で許可しない設定
            urlConnection.setInstanceFollowRedirects(false);

            // ヘッダーの設定(複数設定可能)
            urlConnection.setRequestProperty("Accept-Language", "jp");

            // 接続
            urlConnection.connect();

            int resp = urlConnection.getResponseCode();

            switch (resp) {

                case HttpURLConnection.HTTP_OK:
                    InputStream is = urlConnection.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                    is.close();
                    e_print("switch >> HttpURLConnection.HTTP_OK");
                    break;

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    e_print("switch >> HttpURLConnection.HTTP_UNAUTHORIZED");
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e_print("downloadImage error");
            e.printStackTrace();
        }

        return bmp;
    }
}
