package com.intelisys.backgroundlocation;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteController  extends SQLiteOpenHelper {
    public SqliteController(Context applicationcontext) {
        super(applicationcontext, "androidsqlite.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query; query = "CREATE TABLE IF NOT EXISTS gender( genderId INTEGER PRIMARY KEY, status INTEGER)";
        db.execSQL(query);
        Log.d("Vkk_dev","gender table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query; query = "DROP TABLE IF EXISTS gender";
        db.execSQL(query);
        onCreate(db);
    }
    public void insertgender(Integer status) {
        Log.d("VKK_dev","the gender is "+status);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO gender (genderId,status) VALUES(1,"+status.toString()+")");
    }
    public int getgender() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String selectQuery = "SELECT * FROM gender;";
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Log.d("Vkk_dev", "the db value is : " + cursor.getString(1));
                } while (cursor.moveToNext());
            }
            return 1;
        }
        catch (Exception ex){
            System.out.println("entered the catch segment");
            return 0;
        }
    }

}

