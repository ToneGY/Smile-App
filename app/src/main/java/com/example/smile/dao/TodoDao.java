package com.example.smile.dao;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;

import com.example.smile.constants.Constants;
import com.example.smile.entity.TodoEntity;
import com.example.smile.sqlite.ColumnTodoList;
import com.example.smile.sqlite.DBTemplate;

import java.util.List;

public class TodoDao {
    private DBTemplate<TodoEntity> template = null;//  = new DBTemplate<>();
    private final TodoCallback callback = new TodoCallback();

    public TodoDao(Context context){
        template = new DBTemplate<TodoEntity>(context);
    }

    public List<TodoEntity> findAll(){
        String sql = "select * from " + ColumnTodoList.DB_TABLE + ";";
        return template.query(sql, callback);
    }

    public void update(TodoEntity entity){
        template.update(ColumnTodoList.DB_TABLE, generateContentValues(entity), BaseColumns._ID + "= ?", Integer.toString(entity.getId()));
    }

    public void create(TodoEntity note) {
        template.create(ColumnTodoList.DB_TABLE, generateContentValues(note));
    }

    public void delete(List<Integer> list){
        for (Integer i : list) {
            template.remove(ColumnTodoList.DB_TABLE, BaseColumns._ID + " = ?", i.toString());
        }
    }

    public List<TodoEntity> query(String sql){
        return template.query(sql, callback);
    }

    public TodoEntity getMinUndoEntity() {
        String sql = "select * from " + ColumnTodoList.DB_TABLE + " where " + ColumnTodoList.KEY_STATE + " !=? " + " and "+ ColumnTodoList.KEY_STATE  + " !=? " +  " order by " + ColumnTodoList.KEY_END_TIME + "ASC limit 1;";
        return template.queryOne(sql, callback, String.valueOf(Constants.NICE.OVERDUE), String.valueOf(Constants.NICE.DONE));
    }

    public Integer getNewId(){
        String sql = "select * from " + ColumnTodoList.DB_TABLE + " order by " + BaseColumns._ID + " desc limit 1;";
        return template.queryOne(sql, callback).getId();
    }

    private ContentValues generateContentValues(TodoEntity note) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ColumnTodoList.KEY_TITLE, note.getTitle());
        contentValues.put(ColumnTodoList.KEY_CONTENT, note.getContent());
        contentValues.put(ColumnTodoList.KEY_BEGIN_TIME, note.getBegin_time());
        contentValues.put(ColumnTodoList.KEY_END_TIME, note.getEnd_time());
        contentValues.put(ColumnTodoList.KEY_NICE, note.getNice());
        contentValues.put(ColumnTodoList.KEY_STATE,note.getState());
        contentValues.put(ColumnTodoList.KEY_LABEL, note.getLabel());
        return contentValues;
    }


}

