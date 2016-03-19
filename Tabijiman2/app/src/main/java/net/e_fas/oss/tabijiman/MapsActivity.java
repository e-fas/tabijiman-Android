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
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SPARQLDelegate, LocationListener {

    // Japanese >> ja, ja_JP
    // English >> en, en_US, en_GB
    public static float zoomLevel = 11.0f;
    static SQLiteHelper helper;
    static SQLiteDatabase db;
    static GoogleMap mMap = null;
    static SupportMapFragment mapFragment;
    static ImageButton TrainButton;
    static ImageButton CarButton;
    static ImageButton WalkButton;
    static ImageButton FrameSwitch;
    static ImageButton PlaceSwitch;
    static ImageButton GoFukuiButton;
    static ImageButton TakePicture;
    static ImageButton FrameCollectionButton;
    static List<ImageButton> buttons;
    static List<Marker> PlaceMarker = new ArrayList<>();
    static List<Marker> FrameMarker = new ArrayList<>();
    static List<MarkerOptions> PlaceMarkerOptions = new ArrayList<>();
    static List<MarkerOptions> FrameMarkerOptions = new ArrayList<>();
    public LocationManager mLocationManager;
    public List<String> providers;
    public LatLng nowLocation = new LatLng(36.0614444, 136.2229937);

    // SDCard のルートディレクトリを取得(Android 用)
    public static File getSDCardDir() {
        return Environment.getExternalStorageDirectory();
    }

    public void print(Object object) {
        Log.d("d_tabiziman", object.toString());
    }

    public void e_print(Object object) {
        Log.e("d_tabiziman", object.toString());
    }

    private View getActionBarView() {

        // 表示するlayoutファイルの取得
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.titlebar, null);

        // CustomViewにクリックイベントを登録する
        ImageButton info = (ImageButton) view.findViewById(R.id.imageButton);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent info = new Intent(getApplicationContext(), Credit.class);
                startActivity(info);
            }
        });
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        e_print("onCreate");

        // ActionBarの設定
        if (savedInstanceState == null) {
            // customActionBarの取得
            View customActionBarView = this.getActionBarView();

            // ActionBarの取得
            ActionBar actionBar = this.getSupportActionBar();

            // 戻るボタンを表示するかどうか('<' <- こんなやつ)
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

        // アプリで使用されているロケール情報を取得
        Locale locale = Locale.getDefault();
        if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE)) {
            locale = Locale.JAPAN;
        } else {
            locale = Locale.ENGLISH;
        }

        // 新しいロケールを設定
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        // Resourcesに対するロケールを設定
        config.locale = locale;
        Resources resources = getBaseContext().getResources();
        // Resourcesに対する新しいロケールを反映
        resources.updateConfiguration(config, null);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        try {
            new CreateInitData(this).exec(locale);
            new SPARQL().query(AppSetting.query_place, "place");
            new SPARQL().query(AppSetting.query_frame, "frame");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        makeTempDir();

        // ネットワーク環境の確認
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), "ネットワーク接続がありません。データを取得できません", Toast.LENGTH_LONG).show();
        }


        buttons = new ArrayList<>();

        View.OnClickListener change_button = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CameraPosition cameraPos;

                if (v == TrainButton) {

                    zoomLevel = 9.0f;

                    cameraPos = new CameraPosition.Builder()
                            .target(new LatLng(nowLocation.latitude, nowLocation.longitude)).zoom(zoomLevel).bearing(0).build();
                } else if (v == CarButton) {

                    zoomLevel = 11.0f;

                    cameraPos = new CameraPosition.Builder()
                            .target(new LatLng(nowLocation.latitude, nowLocation.longitude)).zoom(zoomLevel).bearing(0).build();
                } else {

                    zoomLevel = 14.0f;

                    cameraPos = new CameraPosition.Builder()
                            .target(new LatLng(nowLocation.latitude, nowLocation.longitude)).zoom(zoomLevel).bearing(0).build();
                }

                if (!v.isActivated()) {

                    v.setActivated(true);

                    for (int i = 0; i < buttons.size(); i++) {

                        if (buttons.get(i) != v) {
                            buttons.get(i).setActivated(false);
                        }
                    }
                }

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            }
        };

        View.OnClickListener marker_change_button = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isActivated()) {

                    v.setActivated(true);
                    if (v == FrameSwitch) {

                        setMarker("frame");

                        FrameMarkerOptions.clear();
                    } else {

                        setMarker("place");

                        PlaceMarkerOptions.clear();
                    }
                } else {

                    v.setActivated(false);
                    if (v == FrameSwitch) {

                        for (Marker m : FrameMarker) {
                            m.remove();
                        }

                        FrameMarker.clear();
                    } else {
                        for (Marker m : PlaceMarker) {
                            m.remove();
                        }

                        PlaceMarker.clear();
                    }
                }
            }
        };

        // LocationManagerを取得
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        TakePicture = (ImageButton) findViewById(R.id.takePicture);
        TakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent TakePictureView = new Intent(getApplicationContext(), TakePicture.class);
                startActivity(TakePictureView);
            }
        });

        TrainButton = (ImageButton) findViewById(R.id.Train);
        TrainButton.setActivated(false);
        TrainButton.setOnClickListener(change_button);

        CarButton = (ImageButton) findViewById(R.id.Car);
        CarButton.setActivated(true);
        CarButton.setOnClickListener(change_button);

        WalkButton = (ImageButton) findViewById(R.id.Walk);
        WalkButton.setActivated(false);
        WalkButton.setOnClickListener(change_button);

        buttons = Arrays.asList(TrainButton, CarButton, WalkButton);

        FrameSwitch = (ImageButton) findViewById(R.id.showFrame);
        FrameSwitch.setActivated(true);
        FrameSwitch.setOnClickListener(marker_change_button);

        PlaceSwitch = (ImageButton) findViewById(R.id.showPlace);
        PlaceSwitch.setActivated(true);
        PlaceSwitch.setOnClickListener(marker_change_button);

        GoFukuiButton = (ImageButton) findViewById(R.id.GoFukuiButton);

//        new AppSetting(this);
//        AppSetting.context = getApplicationContext();
        AppSetting.Inc_CountRun();
        e_print("Run_Count >> " + AppSetting.CountRun());

        if (AppSetting.CountRun() % 20 == 0) {
            GoFukuiButton.setVisibility(View.VISIBLE);
        }

        FrameCollectionButton = (ImageButton) findViewById(R.id.frameCollection);
        FrameCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent collection = new Intent(getApplicationContext(), FrameCollection.class);
                startActivity(collection);
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        mapFragment.getMap();

        helper = new SQLiteHelper(this);
        db = helper.getWritableDatabase();

    }

    // SDCard 内の絶対パスに変換(Android 用)
    public void makeTempDir() {

        File file = new File(getSDCardDir().getAbsolutePath() + File.separator + getResources().getString(R.string.dir));
        if (!file.exists()) {
            boolean t = file.mkdirs();
            e_print("mkdir >> " + t);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        e_print("onStart");
    }

    @Override
    protected void onResume() {

        if (mLocationManager != null) {

            e_print("onResume_mLocationManager >> NotNull");

            providers = mLocationManager.getProviders(true);
        } else {

            e_print("onResume_mLocationManager >> null");
        }

        super.onResume();
        e_print("onResume");

        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        e_print("last_location >> " + location);
    }

    @Override
    protected void onStop() {

        super.onStop();
        mLocationManager.removeUpdates(this);
        e_print("LocationManager_onStop >> removeUpdates");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        e_print("onMapReady");
        mMap = googleMap;

        GoFukuiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraPosition cameraPos = new CameraPosition.Builder()
                        .target(new LatLng(36.0642988, 136.220012)).zoom(10.0f)
                        .bearing(0).build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                GoFukuiButton.setVisibility(View.GONE);
            }
        });

        CameraPosition cameraPos = new CameraPosition.Builder()
                .target(new LatLng(36.0614444, 136.2229937)).zoom(zoomLevel)
                .bearing(0).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

        // タップ時のイベントハンドラ登録
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View view = getLayoutInflater().inflate(R.layout.info_window, null);
                // タイトル設定
                TextView title = (TextView) view.findViewById(R.id.info_title);
                title.setText(marker.getTitle());
                e_print("marker_Snippet >> " + marker.getSnippet());

                if (marker.getSnippet() == null) {
                    view.findViewById(R.id.info_address).setVisibility(View.GONE);
                } else {
                    TextView address = (TextView) view.findViewById(R.id.info_address);
                    address.setText(marker.getSnippet());
                }
                return view;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                e_print("title_name >> " + marker.getTitle());

                //DB(読出用)のオブジェクト生成
                SQLiteDatabase dbRead = helper.getReadableDatabase();

                //SQL文
                String select_frame_sql = "SELECT * FROM frame WHERE `name` = ? AND `lat` = ? AND `lng` = ?";
                String select_place_sql = "SELECT * FROM place WHERE `name` = ? AND `lat` = ? AND `lng` = ?";
                //SQL文の実行
                Cursor cursor = dbRead.rawQuery(select_place_sql,
                        new String[]{marker.getTitle(), String.valueOf(marker.getPosition().latitude), String.valueOf(marker.getPosition().longitude)});

                e_print("cursor_count >> " + cursor.getCount());

                if (cursor.getCount() != 0) {

                    cursor.moveToFirst();

                    e_print("lat >> " + marker.getPosition().latitude + " lng >> " + marker.getPosition().longitude);


                    Intent MoreInfo = new Intent(getApplicationContext(), MoreInfo.class);
                    MoreInfo.putExtra("id", cursor.getInt(cursor.getColumnIndex("_id")));
                    MoreInfo.putExtra("cat", "place");

                    cursor.close();

                    startActivity(MoreInfo);
                } else {

                    cursor.close();

                    //SQL文の実行
                    Cursor cursor2 = dbRead.rawQuery(select_frame_sql,
                            new String[]{marker.getTitle(), String.valueOf(marker.getPosition().latitude), String.valueOf(marker.getPosition().longitude)});
                    cursor2.moveToFirst();

                    /*
                     * distance[0] = [２点間の距離]
                     * distance[1] = [始点から見た方位角]
                     * distance[2] = [終点から見た方位角]
                     */
                    float[] distance = getDistance(nowLocation.latitude, nowLocation.longitude, marker.getPosition().latitude, marker.getPosition().longitude);


                    Intent MoreInfo = new Intent(getApplicationContext(), MoreInfo.class);
                    MoreInfo.putExtra("dist", distance[0]);
                    MoreInfo.putExtra("id", cursor2.getInt(cursor2.getColumnIndex("_id")));
                    MoreInfo.putExtra("cat", "frame");

                    cursor2.close();

                    startActivity(MoreInfo);
                }


            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                e_print("MyLocation >> " + location);

                Toast.makeText(getApplicationContext(), "location >> " + location, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mMap.setMyLocationEnabled(true);
        // MyLocationButtonを有効に
        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);

        //現在地表示ボタン取得
        View locationButton = ((View) super.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 180, 180);

        print("mMap >> insert");

        for (String provider : providers) {

            e_print("enable_providers >> " + provider);
            mLocationManager.requestLocationUpdates(provider, 3000, 0, this);
        }

        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            e_print("Provider_enable >> NETWORK");

            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    10000,
                    0,
                    this);

        } else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            e_print("Providers_enable >> GPS");

            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0,
                    this);
        } else {

            e_print("All_Providers_Disable");

        }
    }

    @Override
    public void onLocationChanged(Location location) {

        e_print("nowLocation >> " + nowLocation);

        e_print("onLocationChanged");
        print("LocationChanged >> " + location);
        nowLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        e_print("onStatusChanged");

        switch (status) {
            case LocationProvider.AVAILABLE:
                e_print("AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                e_print("OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                e_print("TEMPORARILY_UNAVAILABLE");
                break;

        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void parsed(ArrayList<HashMap> data, String category) {

        String Hash = AppSetting.Encrypt(String.valueOf(data));

        switch (category) {
            case "frame":
                if (AppSetting.isHashChanged(Hash, AppSetting.getUdFrameHash())) {

                    e_print("make a new frame_dic in delegate.parsed");
                    AppSetting.setUdFrameHash(Hash);

                } else {
                    e_print("Already Inserted Frame Data");
                    setMarker(category);
                    return;
                }
            case "place":
                if (AppSetting.isHashChanged(Hash, AppSetting.getUdPlaceHash())) {

                    e_print("make a new place_dic in delegate.parsed");
                    AppSetting.setUdPlaceHash(Hash);

                } else {
                    e_print("Already Inserted Place Data");
                    setMarker(category);
                    return;
                }
        }

        String sql;
        print("Success");
        print("category >> " + category);

        if (category.equals("frame")) {

            e_print("delete all data from frameTable");
            db.execSQL("delete from frame");

        } else {

            e_print("delete all data from placeTable");
            db.execSQL("delete from place");
        }


        for (HashMap test : data) {

            if (category.equals("frame") && !test.containsKey("status")) {

                print("switch - frame");

                String name = test.containsKey("name") ? test.get("name").toString() : "";
                String desc = test.containsKey("desc") ? test.get("desc").toString() : "";
                String lat = test.containsKey("lat") ? test.get("lat").toString() : "";
                String lng = test.containsKey("lng") ? test.get("lng").toString() : "";
                String img = test.containsKey("img") ? test.get("img").toString() : "";
                String area = test.containsKey("area") ? test.get("area").toString() : "";

                sql = "REPLACE INTO frame( name, desc, lat, lng, img, area, getFlag ) VALUES("
                        + DatabaseUtils.sqlEscapeString(name) + ","
                        + DatabaseUtils.sqlEscapeString(desc) + ","
                        + DatabaseUtils.sqlEscapeString(lat) + ","
                        + DatabaseUtils.sqlEscapeString(lng) + ","
                        + DatabaseUtils.sqlEscapeString(img) + ","
                        + DatabaseUtils.sqlEscapeString(area) + ","
                        + DatabaseUtils.sqlEscapeString(String.valueOf(0))
                        + ")";

                print("SQL_QUERY >> " + sql);

                //DB(読出用)のオブジェクト生成
                SQLiteDatabase dbRead = helper.getReadableDatabase();
                //SQL文
                String select_sql = "SELECT `name`, `lat`, `lng` FROM frame WHERE `name` = ? AND `lat` = ? AND `lng` = ?";
                //SQL文の実行
                Cursor cursor = dbRead.rawQuery(select_sql, new String[]{name, lat, lng});
                //カーソル開始位置を先頭にする
                cursor.moveToFirst();

                if (cursor.getCount() == 0) {
                    db.execSQL(sql);
                    print("frame_insert >> true");
                } else {
                    print("frame_insert >> false");
                }

                cursor.close();
            } else if (category.equals("place")) {

                print("switch - place");

                String name = test.containsKey("name") ? test.get("name").toString() : "";
                String address = test.containsKey("address") ? test.get("address").toString() : "";
                String desc = test.containsKey("desc") ? test.get("desc").toString() : "";
                String lat = test.containsKey("lat") ? test.get("lat").toString() : "";
                String lng = test.containsKey("lng") ? test.get("lng").toString() : "";
                String img = test.containsKey("img") ? test.get("img").toString() : "";

                sql = "REPLACE INTO place( name, address, desc, lat, lng, img, getFlag ) VALUES("
                        + DatabaseUtils.sqlEscapeString(name) + ","
                        + DatabaseUtils.sqlEscapeString(address) + ","
                        + DatabaseUtils.sqlEscapeString(desc) + ","
                        + DatabaseUtils.sqlEscapeString(lat) + ","
                        + DatabaseUtils.sqlEscapeString(lng) + ","
                        + DatabaseUtils.sqlEscapeString(img) + ","
                        + DatabaseUtils.sqlEscapeString(String.valueOf(1))
                        + ")";

                print("SQL_QUERY >> " + sql);

                //DB(読出用)のオブジェクト生成
                SQLiteDatabase dbRead = helper.getReadableDatabase();
                //SQL文
                String select_sql = "SELECT `name`, `lat`, `lng` FROM place WHERE `name` = ? AND `lat` = ? AND `lng` = ?";
                print("select_sql >> " + select_sql);
                //SQL文の実行
                Cursor cursor = dbRead.rawQuery(select_sql, new String[]{name, lat, lng});

                print("cursor_count >> " + cursor.getCount());

                if (cursor.getCount() == 0) {
                    db.execSQL(sql);
                    print("place_insert >> true");
                } else {
                    print("place_insert >> false");
                }

                cursor.close();
            }
        }

        print("setMarker >> true");
        setMarker(category);
    }

    public void setMarker(String cat) {

        Cursor cursor;
        MarkerOptions options;

        if (mMap != null) {

            if (cat.equals("frame")) {
                cursor = db.rawQuery("SELECT * FROM frame ORDER BY _id", null);
                cursor.moveToFirst();

                while (cursor.moveToNext()) {

                    options = new MarkerOptions();

                    LatLng location = new LatLng(cursor.getDouble(cursor.getColumnIndex("lat")), cursor.getDouble(cursor.getColumnIndex("lng")));
                    options.position(location);
                    options.title(cursor.getString(cursor.getColumnIndex("name")));
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.top_pin_flame);
                    options.icon(icon);

                    FrameMarkerOptions.add(options);
                    FrameMarker.add(mMap.addMarker(options));
                }

                cursor.close();
            } else if (cat.equals("place")) {

                cursor = db.rawQuery("SELECT * FROM place ORDER BY _id", null);
                cursor.moveToFirst();

                while (cursor.moveToNext()) {

                    options = new MarkerOptions();

                    LatLng location = new LatLng(cursor.getDouble(cursor.getColumnIndex("lat")), cursor.getDouble(cursor.getColumnIndex("lng")));
                    options.position(location);
                    options.title(cursor.getString(cursor.getColumnIndex("name")));
                    options.snippet(cursor.getString(cursor.getColumnIndex("address")));
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.top_pin_info);
                    options.icon(icon);

                    PlaceMarkerOptions.add(options);
                    PlaceMarker.add(mMap.addMarker(options));
                }

                cursor.close();
            }
        } else {

            print("mMap >> NULL");
        }
    }

    /*
     * 2点間の距離（メートル）、方位角（始点、終点）を取得
     * ※配列で返す[距離、始点から見た方位角、終点から見た方位角]
     */
    public float[] getDistance(double x, double y, double x2, double y2) {

        // 結果を格納するための配列を生成
        float[] results = new float[3];

        // 距離計算
        Location.distanceBetween(x, y, x2, y2, results);

        return results;
    }
}

class SPARQL extends MapsActivity {

    public void query(String query, String category) throws UnsupportedEncodingException {

        String url = "http://sparql.odp.jig.jp/api/v1/sparql";
        url += "?output=json&query=" + URLEncoder.encode(query, "UTF-8");

        print("query >> " + query);
        print("url >> " + url);

        new ExecuteSPARQL(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, category);
    }
}

//    AsyncTask<Param, Progress, Result>
//    onPreExecute()	事前準備の処理を記述する
//    doInBackground(Params...)	バックグラウンドで行う処理を記述する
//    onProgressUpdate(Progress...)	進捗状況をUIスレッドで表示する処理を記述する
//    onPostExecute(Result)	バックグラウンド処理が完了し、UIスレッドに反映する処理を記述する
class ExecuteSPARQL extends AsyncTask<String, Integer, Integer> {

    ArrayList<HashMap> data = new ArrayList<>();
    String category = "";
    HashMap<String, String> temp = new HashMap<>();
    private SPARQLDelegate callback = null;

    // コンストラクタ
    public ExecuteSPARQL(SPARQLDelegate callback) {
        this.callback = callback;
    }

    public void print(Object object) {
        Log.d("d_tabiziman", object.toString());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (category.equals("frame")) {
            print("----Execute frame----");
            print("query_frame >> " + AppSetting.query_frame);
        } else {
            print("----Execute place----");
            print("query_place >> " + AppSetting.query_place);
        }
        print("callback data >> " + data);
        callback.parsed(data, category);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Integer doInBackground(String... params) {

        category = params[1];
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(String.valueOf(params[0]));
        HttpResponse httpResponse;

        try {
            httpResponse = httpClient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        int status = httpResponse.getStatusLine().getStatusCode();
        print("status >> " + status);


        if (HttpStatus.SC_OK == status) {

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                httpResponse.getEntity().writeTo(outputStream);

                JSONObject rootObject = new JSONObject(outputStream.toString());

                JSONArray dataArray = rootObject.getJSONObject("results").getJSONArray("bindings");

                for (int i = 0; i < dataArray.length(); i++) {

                    temp = new HashMap<>();
                    temp.put("name", dataArray.getJSONObject(i).getJSONObject("name").getString("value"));
                    temp.put("desc", dataArray.getJSONObject(i).getJSONObject("desc").getString("value"));
                    temp.put("img", dataArray.getJSONObject(i).getJSONObject("img").getString("value"));
                    temp.put("lat", dataArray.getJSONObject(i).getJSONObject("lat").getString("value"));
                    temp.put("lng", dataArray.getJSONObject(i).getJSONObject("lng").getString("value"));

                    if (!dataArray.getJSONObject(i).isNull("rad")) {
                        temp.put("area", dataArray.getJSONObject(i).getJSONObject("rad").getString("value"));
                    }

                    if (!dataArray.getJSONObject(i).isNull("add")) {
                        temp.put("address", dataArray.getJSONObject(i).getJSONObject("add").getString("value"));
                    }

                    data.add(temp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return 0;
        }
        return null;
    }
}

class CreateInitData extends MapsActivity {

    static HashMap<String, String> temp;
    static ArrayList<HashMap> data = new ArrayList<>();
    // ファイルの読み込み
    static InputStream input;
    static AssetManager assetManager;
    static String jsonString = null;
    static JSONObject json = null;
    static Context Init_context;
    static SQLiteHelper Init_helper;
    static SQLiteDatabase Init_db;

    public CreateInitData(Context context) {
        Init_context = context;
        Init_helper = new SQLiteHelper(Init_context);
    }

    public void InsertFrame(ArrayList<HashMap> datas) {

        Init_db = Init_helper.getWritableDatabase();
        Init_db.execSQL("delete from init");
        String sql;

        for (HashMap test : datas) {

            print("switch - init");
            String name = test.containsKey("name") ? test.get("name").toString() : "";
            String desc = test.containsKey("desc") ? test.get("desc").toString() : "";
            String lat = test.containsKey("lat") ? test.get("lat").toString() : "";
            String lng = test.containsKey("lng") ? test.get("lng").toString() : "";
            String img = test.containsKey("img") ? test.get("img").toString() : "";
            String area = test.containsKey("area") ? test.get("area").toString() : "";

            sql = "REPLACE INTO init( name, desc, lat, lng, img, area, getFlag ) VALUES("
                    + DatabaseUtils.sqlEscapeString(name) + ","
                    + DatabaseUtils.sqlEscapeString(desc) + ","
                    + DatabaseUtils.sqlEscapeString(lat) + ","
                    + DatabaseUtils.sqlEscapeString(lng) + ","
                    + DatabaseUtils.sqlEscapeString(img) + ","
                    + DatabaseUtils.sqlEscapeString(area) + ","
                    + DatabaseUtils.sqlEscapeString(String.valueOf(1))
                    + ")";

            print("SQL_QUERY >> " + sql);

            Init_db.execSQL(sql);
        }
    }

    public void exec(Locale locale) throws IOException, JSONException {

        JSONArray datas;
        String line;
        data = new ArrayList<>();

        assetManager = Init_context.getResources().getAssets();
        // assets/initFrameJSON.jsonを読む
        input = assetManager.open("initFrameJSON.json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder stringBuilder = new StringBuilder();


        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        jsonString = stringBuilder.toString();
        json = new JSONObject(jsonString);

        String Hash = AppSetting.Encrypt(String.valueOf(json));
        e_print("new Hash >> " + Hash + "\nstore Hash >> " + AppSetting.getUdInitHash());

        if (AppSetting.isHashChanged(Hash, AppSetting.getUdInitHash())) {

            e_print("make a new init data");
            AppSetting.setUdInitHash(Hash);

            if (locale.equals(Locale.JAPAN)) {

                datas = json.getJSONObject("results").getJSONObject("bindings").getJSONArray("jp");
            } else {

                datas = json.getJSONObject("results").getJSONObject("bindings").getJSONArray("en");
            }

            e_print("datas_count >> " + datas.length());

            for (int i = 0; i < datas.length(); i++) {


                temp = new HashMap<>();

                temp.put("name", datas.getJSONObject(i).getJSONObject("name").getString("value"));
                temp.put("desc", datas.getJSONObject(i).getJSONObject("desc").getString("value"));
                temp.put("img", datas.getJSONObject(i).getJSONObject("img").getString("value"));
                temp.put("lat", datas.getJSONObject(i).getJSONObject("lat").getString("value"));
                temp.put("lng", datas.getJSONObject(i).getJSONObject("lng").getString("value"));
                if (!datas.getJSONObject(i).isNull("area")) {
                    temp.put("area", datas.getJSONObject(i).getJSONObject("area").getString("value"));
                }

                print("initFrame_temp " + i + " >> " + temp);

                data.add(temp);
            }

            InsertFrame(data);

        } else {

            e_print("Already Inserted Init Data");

        }
    }
}
