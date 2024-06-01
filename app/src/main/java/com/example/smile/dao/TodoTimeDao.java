package com.example.smile.dao;

import android.util.Log;

import com.example.smile.entity.TodoEntity;

import java.util.List;

public class TodoTimeDao {
    TodoDao todoDao;


    public TodoTimeDao(TodoDao todoDao) {
        this.todoDao = todoDao;
    }

    public String getMinTimeBackTime(){
        String sql = "select * from todo order by begin_time asc limit 1";
        List<TodoEntity> entityList = todoDao.query(sql);
        return entityList.get(0).getBegin_time();
    }

    public String getMaxTimeBackTime(){
        String sql = "select * from todo order by end_time desc limit 1";
        List<TodoEntity> entityList = todoDao.query(sql);
        return entityList.get(0).getEnd_time();
    }

    public int getCount(String time, int nice){
        String t = time.substring(0, time.lastIndexOf(" "));
        String sql = "select * from todo where ((begin_time <= \""+ time + "\" and end_time >= \""+ time+"\") or (begin_time like \""+ t+ "%\" or end_time like \"" + t + "%\")) and nice = " + nice;
        List<TodoEntity> entityList = todoDao.query(sql);
        return entityList == null ? 0 : entityList.size();
    }

    public List<TodoEntity> getEntityList(String time, int nice, boolean haveNice){
        String t = time.substring(0, time.lastIndexOf(" "));
        String sql;
        if(haveNice) sql = "select * from todo where ((begin_time <= \""+ time + "\" and end_time >= \""+ time+"\") or (begin_time like \""+ t+ "%\" or end_time like \"" + t + "%\")) and nice = " + nice +" order by nice desc";
        else sql = "select * from todo where (begin_time <= \""+ time + "\" and end_time >= \""+ time+"\") or (begin_time like \""+ t+ "%\" or end_time like \"" + t + "%\") order by nice desc";
        Log.e("sql",sql);
        return todoDao.query(sql);
    }
}
