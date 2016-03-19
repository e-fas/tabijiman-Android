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

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class CustomData {

    private Bitmap imageData;
    private String TitleLabel;
    private String DescLabel;
    private boolean isSection;
    private String SectionName;
    private Integer InsertNumber;

    public String getSectionName() {
        return SectionName;
    }

    public void setSectionName(String name) {
        SectionName = name;
    }

    public boolean getIsSection() {
        return isSection;
    }

    public void setIsSection(boolean section) {
        isSection = section;
    }

    public Bitmap getImageData() {
        return imageData;
    }

    public void setImageData(Bitmap image) {
        imageData = image;
    }

    public String getTitleLabel() {
        return TitleLabel;
    }

    public void setTitleLabel(String text) {
        TitleLabel = text;
    }

    public String getDescLabel() {
        return DescLabel;
    }

    public void setDescLabel(String text) {
        DescLabel = text;
    }

    public Integer getInsertNumber() {
        return InsertNumber;
    }

    public void setInsertNumber(Integer Num) {
        InsertNumber = Num;
    }
}

public class FrameCollection extends AppCompatActivity {

    static Cursor initCursor;
    static Cursor getFrameCursor;
    static List getFrameName;
    static int initFrameNum;
    static int getFrameNum;
    static int initFrameSection;
    static int getFrameSection;
    static int SectionCounter = 0;

    public void e_print(Object object) {
        Log.e("d_tabiziman", object.toString());
    }

    private View getActionBarView() {

        // 表示するlayoutファイルの取得
        LayoutInflater inflater = LayoutInflater.from(this);
        return inflater.inflate(R.layout.collection_bar, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_collection);

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

        SQLiteHelper helper = new SQLiteHelper(this);
        //DB(読出用)のオブジェクト生成
        SQLiteDatabase dbRead = helper.getReadableDatabase();
        //SQL文
        final String init_select_sql = "SELECT * FROM init WHERE 1";
        String getFrame_select_sql = "SELECT * FROM getFrame WHERE 1";
        //SQL文の実行
        initCursor = dbRead.rawQuery(init_select_sql, null);
        getFrameCursor = dbRead.rawQuery(getFrame_select_sql, null);

        initFrameNum = initCursor.getCount();
        getFrameNum = getFrameCursor.getCount();

        initCursor.close();
        getFrameCursor.close();

        getFrameName = AppSetting.GetFileNames();

        e_print("getFrameName >> " + getFrameName);

        // データの作成
        List<CustomData> objects = new ArrayList<>();

        for (int i = -1; i < initFrameNum; i++) {

            CustomData init_item = new CustomData();

            if (i == -1) {
                initFrameSection = SectionCounter;
                init_item.setIsSection(true);
                init_item.setSectionName("初期フレーム");
            } else {

                try {
                    BitmapFactory.Options imageOptions = new BitmapFactory.Options();
                    imageOptions.inPreferredConfig = Bitmap.Config.ARGB_4444;

                    InputStream is = getResources().getAssets().open(AppSetting.initFrameName[i]);
                    final Bitmap bm = BitmapFactory.decodeStream(is, null, imageOptions);

                    e_print("initFrameName_" + i + " >> " + AppSetting.initFrameName[i]);
                    String name = AppSetting.initFrameName[i];
                    GetCollectionData(name, "init");

                    init_item.setImageData(bm);
                    init_item.setTitleLabel(initCursor.getString(initCursor.getColumnIndex("name")));
                    init_item.setDescLabel(initCursor.getString(initCursor.getColumnIndex("desc")));
                    init_item.setInsertNumber(initCursor.getInt(initCursor.getColumnIndex("_id")));
                    initCursor.close();

                } catch (IOException e) {
                    /* 例外処理 */
                    e.printStackTrace();
                }
            }

            objects.add(init_item);
            SectionCounter++;
        }

        for (int j = -1; j < getFrameNum; j++) {

            CustomData getFrame_item = new CustomData();

            if (j == -1) {

                getFrameSection = SectionCounter;
                getFrame_item.setIsSection(true);
                getFrame_item.setSectionName("ゲットフレーム");
                objects.add(getFrame_item);
            } else {

                if (!getFrameName.isEmpty()) {

                    try {
                        BitmapFactory.Options imageOptions = new BitmapFactory.Options();
                        imageOptions.inPreferredConfig = Bitmap.Config.ARGB_4444;

                        final Bitmap temp = AppSetting.loadBitmapSDCard(String.valueOf(getFrameName.get(j)));

                        String FrameName = String.valueOf(getFrameName.get(j));

                        GetCollectionData(FrameName.substring(0, FrameName.length() - 4), "getFrame");

                        getFrame_item.setImageData(temp);
                        getFrame_item.setTitleLabel(getFrameCursor.getString(getFrameCursor.getColumnIndex("name")));
                        getFrame_item.setDescLabel(getFrameCursor.getString(getFrameCursor.getColumnIndex("desc")));
                        getFrame_item.setInsertNumber(getFrameCursor.getInt(getFrameCursor.getColumnIndex("_id")));
                        getFrameCursor.close();

                    } catch (IOException e) {
                            /* 例外処理 */
                        e.printStackTrace();
                    }

                    objects.add(getFrame_item);
                    SectionCounter++;
                }
            }
        }

        CustomAdapter customAdapater = new CustomAdapter(this, 0, objects);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(customAdapater);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                e_print("isEnabled(position) >> " + isEnabled(position));

                if (isEnabled(position)) {

                    Intent info = new Intent(getApplicationContext(), MoreInfo.class);
                    ListView listView = (ListView) parent;
                    CustomData Data = (CustomData) listView.getItemAtPosition(position);

                    if (position < getFrameSection) {

                        info.putExtra("cat", "init");
                        info.putExtra("id", Data.getInsertNumber());
                        info.putExtra("imageName", AppSetting.initFrameName[position - 1]);
                    } else {

                        info.putExtra("cat", "getFrame");
                        info.putExtra("id", Data.getInsertNumber());
                        info.putExtra("imageName", String.valueOf(getFrameName.get(position - getFrameSection - 1)));
                    }

                    startActivity(info);
                }
            }
        });

    }

    public boolean isEnabled(int position) {

        return !(position == initFrameSection || position == getFrameSection);
    }

    public void GetCollectionData(String name, String cat) {

        String init_select_sql;
        String getFrame_select_sql;

        SQLiteHelper helper = new SQLiteHelper(this);
        //DB(読出用)のオブジェクト生成
        SQLiteDatabase dbRead = helper.getReadableDatabase();


        //SQL文
        init_select_sql = "SELECT * FROM init WHERE `img` = ?";
        getFrame_select_sql = "SELECT * FROM getFrame WHERE `name` = ?";


        if (cat.equals("init")) {

            //SQL文の実行
            initCursor = dbRead.rawQuery(init_select_sql, new String[]{name});

            //カーソル開始位置を先頭にする
            initCursor.moveToFirst();
        } else {

            getFrameCursor = dbRead.rawQuery(getFrame_select_sql, new String[]{name});

            getFrameCursor.moveToFirst();
        }
    }
}

