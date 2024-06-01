package com.example.smile.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.smile.activity.MainActivity;
import com.example.smile.entity.Elearning.AssignmentEntity;
import com.example.smile.entity.Elearning.CourseEntity;
import com.example.smile.entity.Elearning.SerializableHashMap;
import com.example.smile.fragment.NoteFragment;
import com.example.smile.fragment.NoteListFragment;
import com.example.smile.util.ElearningUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ElearningService extends Service {
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
//                    try{
                    ElearningUtil.all_init(user_name, passwd);
//                    ElearningUtil.init(ElearningService.this);
//                    ElearningUtil.login(user_name, passwd);
                    List<CourseEntity> ce_list = ElearningUtil.dash();
                    HashMap<Integer, List<AssignmentEntity>> hashMap = new HashMap<>();
                    for (CourseEntity ce : ce_list) {
                        List<AssignmentEntity> as_list = ElearningUtil.getHomework(ce.href);
                        Collections.reverse(as_list);
                        hashMap.put(ce.id, as_list);
                    }
                    SerializableHashMap s_hm = new SerializableHashMap();
                    s_hm.setMap(hashMap);
                    Intent intent = new Intent(NoteFragment.NOTE_BROADCAST_FLAG);
                    intent.putExtra("courseList", (Serializable) ce_list);
                    intent.putExtra("assignmentList", (Serializable) s_hm);
                    sendBroadcast(intent);
//                }
//                    catch (Exception e){
//                        e.printStackTrace();
//                        Looper.prepare();
//                        Toast.makeText(MainActivity.getInstance(), "登录异常，请重新登录",Toast.LENGTH_LONG).show();
//                        Looper.loop();
//                    }
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
