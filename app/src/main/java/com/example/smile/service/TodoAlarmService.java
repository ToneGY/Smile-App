package com.example.smile.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.smile.R;
import com.example.smile.activity.TodoContentActivity;
import com.example.smile.constants.FileName;
import com.example.smile.entity.TodoEntity;
import com.example.smile.util.FileUtil;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

public class TodoAlarmService extends Service {
    public final static String TODO_ALARM_SERVICE_RECEIVE_ENTITY = "todo.alarm.service.receive.entity";
    static class MyEntry implements Map.Entry<Integer,Long> , Comparable<MyEntry>{
        Integer key;
        Long value;
        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        public Long getValue() {
            return value;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        @Override
        public Long setValue(Long aLong) {
            Long old = value;
            value = aLong;
            return old;
        }

        @Override
        public String toString() {
            return "MyEntry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }


        @Override
        public int compareTo(MyEntry myEntry) {
            if(getValue() < myEntry.getValue()) return 1;
            else if(getValue() > myEntry.getValue()) return -1;
            else{
                return getKey().compareTo(myEntry.getKey());
            }
        }
    }

    TreeMap<MyEntry, TodoEntity> entityQueue = new TreeMap<>(new Comparator<MyEntry>() {
        @Override
        public int compare(MyEntry integerLongEntry, MyEntry t1) {
            if(integerLongEntry.getValue() < t1.getValue()) return 1;
            else if(integerLongEntry.getValue() > t1.getValue()) return -1;
            else{
                return integerLongEntry.getKey().compareTo(t1.getKey());
            }
        }
    });
    Map < Integer, Long > mapId = new HashMap<>();
    boolean onWorking = false;
    TodoEntity bindEntity;
    Long alarm_time_entity;

    private String FLAG_ALARM = "todo.alarm.service.flag";


    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    private void EnQueue(Long alarm_time, TodoEntity entity){
        Integer id = entity.getId();
        Long maybeTime = mapId.get(id);
        if(maybeTime != null){
            MyEntry entry = new MyEntry();
            entry.setKey(id);
            entry.setValue(maybeTime);
            entityQueue.remove(entry);
            mapId.remove(id);
        }
        MyEntry entry = new MyEntry();
        entry.setKey(id);
        entry.setValue(alarm_time);
        entityQueue.put(entry, entity);
        mapId.put(id, alarm_time);
    }

    private Map.Entry<MyEntry, TodoEntity> DeQueue(){
        if(entityQueue.isEmpty()) return null;
        Map.Entry<MyEntry, TodoEntity> entry = entityQueue.firstEntry();
        assert entry != null;
        entityQueue.remove(entry.getKey());
        mapId.remove(entry.getKey().getKey());
        bindEntity = entry.getValue();
        return entry;
    }

    private final BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(bindEntity == null) return;
            String channelId = "唤醒服务";
            String endTime = bindEntity.getEnd_time();
            String title = bindEntity.getTitle();
            String contentText = "您有一个待办提醒："+ title +" \n截止时间 " + endTime;
            Intent intent1 = new Intent(context, TodoContentActivity.class);

            intent1.putExtra(TodoContentActivity.TODO_CONTENT_ENTITY, bindEntity);
            @SuppressLint("UnspecifiedImmutableFlag") Notification notification = new Notification.Builder(context, channelId)
                    .setContentTitle("待办到期提醒")
                    .setWhen(System.currentTimeMillis())
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(PendingIntent.getActivity(context, UUID.randomUUID().hashCode(),intent1,PendingIntent.FLAG_UPDATE_CURRENT))
                    .setStyle(new Notification.BigTextStyle().bigText(contentText).setBigContentTitle("待办到期提醒"))
                    .build();
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelId,"测试渠道名称", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1123, notification);

            setAlarmBroadcast();
        }
    };

    private final BroadcastReceiver entityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TodoEntity entity = (TodoEntity) intent.getParcelableExtra("TodoEntity");
            Long alarm_time = intent.getLongExtra("alarm_time", 0);
            EnQueue(alarm_time, entity);
            if (!onWorking) {
                setAlarmBroadcast();
            }else{
                alarmManager.cancel(pendingIntent);
                EnQueue(alarm_time_entity, bindEntity);
                setAlarmBroadcast();
            }
        }
    };



    private void setAlarmBroadcast(){
        Map.Entry<MyEntry, TodoEntity> entry = DeQueue();
        if (entry != null) {
            alarm_time_entity = entry.getKey().getValue();
            onWorking = true;
            pendingIntent = PendingIntent.getBroadcast(TodoAlarmService.this, 0, new Intent(FLAG_ALARM), 0);
            //Intent intent2 = new Intent(FLAG_ALARM);
            //sendBroadcast(intent2);
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, alarm_time_entity, 100, pendingIntent);
        } else {
            onWorking = false;
        }
    }

    Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate() {
        super.onCreate();
        //注册alarmReceiver
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel("todoAlarmService", "Smile", NotificationManager.IMPORTANCE_NONE);
        notificationManager.createNotificationChannel(mChannel);
        notification = new Notification.Builder(getApplicationContext(), "todoAlarmService").setSmallIcon(R.drawable.icon).setContentTitle("Smile后台运行").build();
        startForeground(1, notification);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        IntentFilter intentFilter = new IntentFilter(FLAG_ALARM);
        registerReceiver(alarmReceiver, intentFilter);

        //注册entityReceiver
        IntentFilter intentFilter1 = new IntentFilter(TODO_ALARM_SERVICE_RECEIVE_ENTITY);
        registerReceiver(entityReceiver, intentFilter1);


        if(FileUtil.fileExists(this,FileName.TodoAlarmService_onWorkingJsonString))
            onWorking = (boolean)JSON.parse(FileUtil.readFile(this, FileName.TodoAlarmService_onWorkingJsonString));
        if(FileUtil.fileExists(this, FileName.TodoAlarmService_entityQueueJsonString))
            entityQueue = (TreeMap<MyEntry, TodoEntity>) JSONObject.parseObject(FileUtil.readFile(this, FileName.TodoAlarmService_entityQueueJsonString), TreeMap.class);
        if(FileUtil.fileExists(this,FileName.TodoAlarmService_mapIdJsonString)){
            mapId = (Map<Integer,Long>) JSONObject.parseObject(FileUtil.readFile(this, FileName.TodoAlarmService_mapIdJsonString), Map.class);
        }
        if(FileUtil.fileExists(this,FileName.TodoAlarmService_bindEntityJsonString))
            bindEntity = (TodoEntity) JSONObject.parseObject(FileUtil.readFile(this,FileName.TodoAlarmService_bindEntityJsonString), TodoEntity.class);

        if(onWorking){
            alarm_time_entity = mapId.get(bindEntity.getId());
            pendingIntent = PendingIntent.getBroadcast(TodoAlarmService.this, 0, new Intent(FLAG_ALARM), 0);
            //Intent intent2 = new Intent(FLAG_ALARM);
            //sendBroadcast(intent2);
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, alarm_time_entity, 100, pendingIntent);
        }else{
            setAlarmBroadcast();
        }

    }



    @Override
    public void onDestroy() {
        unregisterReceiver(alarmReceiver);
        unregisterReceiver(entityReceiver);
        String entityQueueJsonString = JSON.toJSONString(entityQueue);
        String mapIdJsonString = JSON.toJSONString(mapId);
        String bindEntityJsonString = JSON.toJSONString(bindEntity);
        String onWorkingJsonString = JSON.toJSONString(onWorking);
        FileUtil.coverAndWrite(TodoAlarmService.this, FileName.TodoAlarmService_entityQueueJsonString, entityQueueJsonString);
        FileUtil.coverAndWrite(TodoAlarmService.this, FileName.TodoAlarmService_mapIdJsonString, mapIdJsonString);
        FileUtil.coverAndWrite(this, FileName.TodoAlarmService_bindEntityJsonString, bindEntityJsonString);
        FileUtil.coverAndWrite(this, FileName.TodoAlarmService_onWorkingJsonString, onWorkingJsonString);
        //startService(new Intent(this, TodoAlarmService.class));
        super.onDestroy();
        alarmManager.cancel(pendingIntent);
    }
}
