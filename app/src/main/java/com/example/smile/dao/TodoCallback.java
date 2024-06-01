package com.example.smile.dao;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.example.smile.entity.TodoEntity;
import com.example.smile.sqlite.ColumnTodoList;
import com.example.smile.sqlite.DBCallback;

public class TodoCallback implements DBCallback<TodoEntity> {
    @Override
    public TodoEntity cursorToInstance(Cursor cursor) {
        TodoEntity note = new TodoEntity();
        note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)));
        note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ColumnTodoList.KEY_TITLE)));
        note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(ColumnTodoList.KEY_CONTENT)));
        note.setBegin_time(cursor.getString(cursor.getColumnIndexOrThrow(ColumnTodoList.KEY_BEGIN_TIME)));
        note.setEnd_time(cursor.getString(cursor.getColumnIndexOrThrow(ColumnTodoList.KEY_END_TIME)));
        note.setNice(cursor.getInt(cursor.getColumnIndexOrThrow(ColumnTodoList.KEY_NICE)));
        note.setState(cursor.getInt(cursor.getColumnIndexOrThrow(ColumnTodoList.KEY_STATE)));
        note.setLabel(cursor.getString(cursor.getColumnIndexOrThrow(ColumnTodoList.KEY_LABEL)));
        return note;
    }
}