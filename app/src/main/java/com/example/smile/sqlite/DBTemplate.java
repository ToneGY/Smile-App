package com.example.smile.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class DBTemplate <T>{
    private SQLiteDatabase db;
    private final DBOpenHelper dbHelper;
    public DBTemplate(Context context){
        dbHelper = new DBOpenHelper(context);
    }

    public void open(){
        db = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public T queryOne(String sql, DBCallback<T> callback, String...args){
        T t = null;
        open();
        Cursor cursor = db.rawQuery(sql,args);
        if(cursor != null && cursor.moveToNext()){
            t = callback.cursorToInstance(cursor);
            cursor.close();
        }
        close();
        return t;
    }

    public List<T> query(String sql, DBCallback<T> callback, String...args){
        List<T> list = new ArrayList<>();
        open();
        Cursor cursor = db.rawQuery(sql, args);
        if(cursor!=null){
            while(cursor.moveToNext()){
                T t = callback.cursorToInstance(cursor);
                list.add(t);
            }
            cursor.close();
        }
        close();
        return list;
    }

    public long create(String table, ContentValues values){
        open();
        long r = db.insert(table, null, values);
        close();
        return r;
    }

    public int remove(String table, String whereConditions, String...args){
        open();
        int r = db.delete(table,whereConditions,args);
        close();
        return r;
    }

    public int getNum(String table){
        String sql = "select num(" + BaseColumns._ID + ") from " + table;
        open();
        Cursor cursor = db.rawQuery(sql, new String[]{});
        int result = -1;
        if(cursor != null && cursor.moveToNext()){
            result = cursor.getInt(0);
            cursor.close();
        }
        close();
        return result;
    }

    public int update(String table, ContentValues contentValues, String whereConditions, String...args){
        open();
        int r = db.update(table,contentValues,whereConditions,args);
        close();
        return r;
    }
}
