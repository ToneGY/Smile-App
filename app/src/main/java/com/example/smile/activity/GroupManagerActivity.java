package com.example.smile.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson2.JSON;
import com.example.smile.R;
import com.example.smile.entity.GroupEntity;
import com.example.smile.entity.UserEntity;
import com.example.smile.fragment.GroupFragment;
import com.example.smile.fragment.MoreFragment;
import com.example.smile.util.SmileUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class GroupManagerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView group_add;
    UserEntity userEntity;
    Adapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.group_manager);
        userEntity = (UserEntity) getIntent().getSerializableExtra("userEntity");
        group_add = findViewById(R.id.group_manager_actionbar_add);
        recyclerView = findViewById(R.id.group_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GroupEntity> groupEntities = SmileUtil.getGroupByUser(userEntity.getAccount());
                if(groupEntities == null) return;
                sendMessage(SET, groupEntities);
            }
        }).start();
        initListener();
    }

    void initListener(){
        group_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View add = LayoutInflater.from(GroupManagerActivity.this).inflate(R.layout.group_add, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GroupManagerActivity.this);
                alertDialogBuilder.setView(add);
                AlertDialog groupAlert = alertDialogBuilder.create();
                groupAlert.setCancelable(false);
                groupAlert.show();
                EditText group_name = add.findViewById(R.id.group_add);
                add.findViewById(R.id.group_add_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = group_name.getText().toString();
                        if(name.trim() == "") return;
                        groupAlert.dismiss();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage(SET,SmileUtil.addGroupByUser(name, userEntity.getAccount()));
                            }
                        }).start();

                    }
                });
            }
        });
    }


    GroupEntity selected_group;

    TextView user_name;
    TextView user_not_exist;
    AlertDialog alertDialog;
    class Adapter extends RecyclerView.Adapter<Adapter.Holder>{

        List<GroupEntity> groupEntities;

        @SuppressLint("NotifyDataSetChanged")
        public void setItems(List<GroupEntity> groupEntities){
            this.groupEntities = groupEntities;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(GroupManagerActivity.this).inflate(R.layout.group_manger_item,parent,false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Holder holder, @SuppressLint("RecyclerView") int position) {
            GroupEntity groupEntity = groupEntities.get(position);
            holder.name.setText(groupEntity.getName());
            holder.id.setText("id: "+ groupEntity.getId());
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_group = groupEntity;
                    View add_account = LayoutInflater.from(GroupManagerActivity.this).inflate(R.layout.group_add_account, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GroupManagerActivity.this);
                    alertDialogBuilder.setView(add_account);
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                    user_name = add_account.findViewById(R.id.group_manger_add_username);
                    user_not_exist = add_account.findViewById(R.id.group_manger_username_not_exist);
                    EditText account = add_account.findViewById(R.id.group_add_user_account);
                    add_account.findViewById(R.id.group_add_user_confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String user_account = account.getText().toString();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    UserEntity userEntity = new UserEntity();
                                    userEntity.setAccount(user_account);
                                    userEntity.setName(SmileUtil.getUserNameByAccount(user_account));
                                    sendMessage(ADD,userEntity);
                                }
                            }).start();
                        }
                    });
                    add_account.findViewById(R.id.group_add_user_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                }
            });
            holder.quit.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    groupEntities.remove(position);
                    notifyDataSetChanged();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SmileUtil.groupDeleteUser(groupEntity.getId(), userEntity.getAccount());
                        }
                    }).start();
                }
            });

        }

        @Override
        public int getItemCount() {
            return groupEntities == null ? 0 : groupEntities.size();
        }

        class Holder extends RecyclerView.ViewHolder{
            CardView add;
            CardView quit;
            TextView name;
            TextView id;
            public Holder(@NonNull View itemView) {
                super(itemView);
                add = itemView.findViewById(R.id.group_manager_item_add);
                quit = itemView.findViewById(R.id.group_manger_item_quit);
                name = itemView.findViewById(R.id.group_manager_item_name);
                id = itemView.findViewById(R.id.group_manager_item_id);
            }

            View getViewHolder(){return itemView;}
        }
    }

    final int ADD = 0;
    final int SET = 1;
    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        private WeakReference<GroupManagerActivity> weakReference;

        public MyHandler(GroupManagerActivity weakReference) {
            this.weakReference = new WeakReference(weakReference);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what==ADD){
                UserEntity userEntity = (UserEntity)msg.obj;
                if(userEntity == null || userEntity.getName() == null || userEntity.getName() ==""){
                    user_not_exist.setVisibility(View.VISIBLE);
                    user_name.setVisibility(View.GONE);
                }else{
                    user_not_exist.setVisibility(View.GONE);
                    user_name.setVisibility(View.VISIBLE);
                    user_name.setText(userEntity.getName());
                    user_name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SmileUtil.goupAddUser(selected_group.getId(), userEntity.getAccount());
                                }
                            }).start();
                            user_not_exist.setVisibility(View.GONE);
                            user_name.setVisibility(View.INVISIBLE);
                            alertDialog.dismiss();
                            Intent intent = new Intent(GroupFragment.NIRVANA);
                            sendBroadcast(intent);
                        }
                    });
                }
            }else if(msg.what == SET){
                List<GroupEntity> list = castList(msg.obj, GroupEntity.class);
                adapter.setItems(list);
                Intent intent = new Intent(GroupFragment.NIRVANA);
                sendBroadcast(intent);
            }

        }
    }

    MyHandler myHandler = new MyHandler(GroupManagerActivity.this);
    void sendMessage(int what, Object obj){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        myHandler.sendMessage(msg);
    }

    public static <T> List<T> castList(Object obj, Class<T> clazz){
        List<T> result = new ArrayList<>();
        if(obj instanceof List<?>){
            for (Object o : (List<?>) obj){
                result.add(clazz.cast(o));
            }
            return result;
        }
        return new ArrayList<>();
    }
}
