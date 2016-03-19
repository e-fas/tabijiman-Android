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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

final class AppSetting extends Application {

    public static Context context = ApplicationController.getInstance().getApplicationContext();

    private static void e_print(Object object) {
        Log.e("d_tabiziman", object.toString());
    }

    public static String[] initFrameName = {
            "fukui_asuwa_oss.png",
            "sabae_nishiyama_oss.png"
    };

    public static String shereTag = "#tabijiman";

    public static String query_place =
            "select ?s ?name ?cat ?lat ?lng ?add ?name ?desc ?img {"
            + "?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/jrrk#CivicPOI>;"
            + "<http://imi.ipa.go.jp/ns/core/rdf#種別> ?cat;"
            + "<http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lng;"
            + "<http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat;"
            + "<http://www.w3.org/2000/01/rdf-schema#label> ?name;"
            + "<http://imi.ipa.go.jp/ns/core/rdf#説明> ?desc;"
            + "<http://schema.org/image> ?img; }";

    public static String query_frame =
            "prefix geo:   <http://www.w3.org/2003/01/geo/wgs84_pos#>"
            + "prefix odp:   <http://odp.jig.jp/odp/1.0#>"
            + "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>"
            + "prefix schema: <http://schema.org/>"
            + "select ?s ?name ?desc ?img ?scope ?center ?lat ?lng ?rads ?rad ?unit {"
            + "?s rdf:type odp:ComicForeground  ."
            + "?s rdfs:label ?name . FILTER ( lang(?name) = \"" + context.getString(R.string.lang) + "\" )"
            + "?s schema:description ?desc . FILTER ( lang(?desc) = \"" + context.getString(R.string.lang) + "\" )"
            + "?s schema:image ?img ."
            + "?s odp:scope ?scope ."
            + "?scope odp:midpoint ?center ."
            + "?center geo:lat ?lat ."
            + "?center geo:long ?lng ."
            + "?scope odp:radius ?rads ."
            + "?rads rdf:value ?rad ."
            + "?rads odp:unit ?unit . }";

    public static boolean isHashChanged(String currentHash, String storedHash) {
        return !currentHash.equals(storedHash);
    }

    public static List<String> GetFileNames() {

        File[] files;
        List<String> frameList = new ArrayList<>();
        e_print("NameGetPath >> " + String.valueOf(context.getFilesDir()));

        files = new File(String.valueOf(context.getFilesDir())).listFiles();

        if (files.length != 0) {

            for (File file : files) {

                if (file.isFile() && file.getName().endsWith(".png")) {

                    if (file.getName().equals("temp.png")) continue;
                    frameList.add(file.getName());
                }
            }
        }

        return frameList;
    }

    //画像ファイルを読み込む
    public static Bitmap loadBitmapSDCard(String fileName) throws IOException {

        BufferedInputStream bis = null;
        e_print("LoadPath >> " + context.getFilesDir() + File.separator + fileName);
        try {
            bis = new BufferedInputStream(new FileInputStream(context.getFilesDir() + File.separator + fileName));
            return BitmapFactory.decodeStream(bis);
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
    }

    //SDCard に、画像ファイルを保存する(png) (Android 用)
    public static boolean savePngSDCard(String fileName, Bitmap bitmap) throws IOException {

        BufferedOutputStream bos = null;
        Bitmap tmp = null;
        e_print("SavePath >> " + context.getFilesDir() + File.separator + fileName);

        try {
            //他アプリアクセス不可
            bos = new BufferedOutputStream(new FileOutputStream(context.getFilesDir() + File.separator + fileName));
            tmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            return tmp.compress(Bitmap.CompressFormat.PNG, 100, bos);

        } finally {
            if (tmp != null) {
                tmp.recycle();
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                //IOException, NullPointerException
                e.printStackTrace();
            }
        }
    }

    public static String Encrypt(String text) {

        // 変数初期化
        MessageDigest md = null;
        StringBuilder buffer = new StringBuilder();

        try {
            // メッセージダイジェストインスタンス取得
            md = MessageDigest.getInstance("SHA-256");

        } catch (NoSuchAlgorithmException e) {
            // 例外発生時、エラーメッセージ出力
            e.printStackTrace();
        }

        // メッセージダイジェスト更新
        if (md != null) {
            md.update(text.getBytes());
        }

        // ハッシュ値を格納
        byte[] valueArray = md != null ? md.digest() : new byte[0];

        // ハッシュ値の配列をループ
        for (byte aValueArray : valueArray) {

            // 値の符号を反転させ、16進数に変換
            String tmpStr = Integer.toHexString(aValueArray & 0xff);

            if (tmpStr.length() == 1) {
                // 値が一桁だった場合、先頭に0を追加し、バッファに追加
                buffer.append('0').append(tmpStr);
            } else {
                // その他の場合、バッファに追加
                buffer.append(tmpStr);
            }
        }

        // 完了したハッシュ計算値を返却
        return buffer.toString();
    }

    public static String getUdPlaceHash() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udPlaceHash = "placeHash";
        return sp.getString(key_udPlaceHash, null);
    }

    public static void setUdPlaceHash(String Hash) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udPlaceHash = "placeHash";

        sp.edit().putString(key_udPlaceHash, Hash).apply();
    }

    public static String getUdFrameHash() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udFrameHash = "frameHash";
        return sp.getString(key_udFrameHash, null);
    }

    public static void setUdFrameHash(String Hash) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udFrameHash = "frameHash";

        sp.edit().putString(key_udFrameHash, Hash).apply();
    }

    public static String getUdInitHash() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udInitHash = "initHash";
        return sp.getString(key_udInitHash, null);
    }

    public static void setUdInitHash(String Hash) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udInitHash = "initHash";

        sp.edit().putString(key_udInitHash, Hash).apply();
    }

    public static void Inc_CountRun() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udCountRun = "CountRun";

        int count = CountRun();
        count++;

        sp.edit().putInt(key_udCountRun, count).apply();
    }

    public static int CountRun() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key_udCountRun = "CountRun";

        return sp.getInt(key_udCountRun, 0);
    }
}
