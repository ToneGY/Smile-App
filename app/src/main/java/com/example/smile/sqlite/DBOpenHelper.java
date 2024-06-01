package com.example.smile.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = DBOpenHelper.class.getSimpleName();
    private static final int VERSION = 1;
    public static final String DB_NAME = "manager.db";

    public final String todo_sql = "create table if not exists " + ColumnTodoList.DB_TABLE + "(" +
            BaseColumns._ID + " integer primary key autoincrement, " +
            ColumnTodoList.KEY_TITLE + " text not null, " +
            ColumnTodoList.KEY_CONTENT + " text, " +
            ColumnTodoList.KEY_BEGIN_TIME + " datetime not null, " +
            ColumnTodoList.KEY_END_TIME + " datetime not null, " +
            ColumnTodoList.KEY_NICE + " integer not null," +
            ColumnTodoList.KEY_STATE + " integer, " +
            ColumnTodoList.KEY_LABEL + " text)";

    public DBOpenHelper(@Nullable Context context) {

        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("sql",todo_sql);
        db.execSQL(todo_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
