package com.example.smile.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.smile.activity.MainActivity;
import com.example.smile.entity.Elearning.AssignmentEntity;
import com.example.smile.entity.Elearning.CourseEntity;
import com.example.smile.entity.Elearning.SerializableHashMap;
import com.example.smile.fragment.NoteFragment;
import com.example.smile.util.ElearningUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ElearningLoginService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences e_load = getSharedPreferences("elearning_load", MODE_PRIVATE);
        String user_name =  e_load.getString("user_name",null);
        String passwd = e_load.getString("passwd",null);
        if(user_name != null && passwd != null && passwd !="" && user_name!="") {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ElearningUtil.all_init(user_name, passwd);
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
