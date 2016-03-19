package net.e_fas.oss.tabijiman;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String INIT_TABLE_NAME = "init";
    public static final String FRAME_TABLE_NAME = "frame";
    public static final String PLACE_TABLE_NAME = "place";
    public static final String GETFRAME_TABLE_NAME = "getFrame";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_DESC = "desc";
    public static final String COL_LAT = "lat";
    public static final String COL_LNG = "lng";
    public static final String COL_IMG = "img";
    public static final String COL_AREA = "area";
    public static final String COL_FLAG = "getFlag";
    static final String DATABASE_NAME = "datas.db";
    static final int DATABASE_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + INIT_TABLE_NAME + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COL_NAME + " TEXT NOT NULL,"
                        + COL_DESC + " TEXT NOT NULL,"
                        + COL_LAT + " REAL NOT NULL,"
                        + COL_LNG + " REAL NOT NULL,"
                        + COL_IMG + " TEXT NOT NULL,"
                        + COL_AREA + " INTEGER NOT NULL,"
                        + COL_FLAG + " INTEGER NOT NULL"
                        + ");"
        );

        db.execSQL(
                "CREATE TABLE " + FRAME_TABLE_NAME + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COL_NAME + " TEXT NOT NULL,"
                        + COL_DESC + " TEXT NOT NULL,"
                        + COL_LAT + " REAL NOT NULL,"
                        + COL_LNG + " REAL NOT NULL,"
                        + COL_IMG + " TEXT NOT NULL,"
                        + COL_AREA + " INTEGER NOT NULL,"
                        + COL_FLAG + " INTEGER NOT NULL"
                        + ");"
        );

        db.execSQL(
                "CREATE TABLE " + PLACE_TABLE_NAME + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COL_NAME + " TEXT NOT NULL,"
                        + COL_ADDRESS + " TEXT NOT NULL,"
                        + COL_DESC + " TEXT NOT NULL,"
                        + COL_LAT + " REAL NOT NULL,"
                        + COL_LNG + " REAL NOT NULL,"
                        + COL_IMG + " TEXT NOT NULL,"
                        + COL_FLAG + " INTEGER NOT NULL"
                        + ");"
        );

        db.execSQL(
                "CREATE TABLE " + GETFRAME_TABLE_NAME + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COL_NAME + " TEXT NOT NULL,"
                        + COL_DESC + " TEXT NOT NULL,"
                        + COL_LAT + " REAL NOT NULL,"
                        + COL_LNG + " REAL NOT NULL,"
                        + COL_IMG + " TEXT NOT NULL,"
                        + COL_AREA + " INTEGER NOT NULL,"
                        + COL_FLAG + " INTEGER NOT NULL"
                        + ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        new MapsActivity().e_print("onUpgrade");
    }
}