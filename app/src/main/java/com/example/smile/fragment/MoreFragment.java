package com.example.smile.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson2.JSON;
import com.example.smile.R;
import com.example.smile.activity.CourseActivity;
import com.example.smile.activity.GroupManagerActivity;
import com.example.smile.activity.MainActivity;
import com.example.smile.entity.UserEntity;
import com.example.smile.service.ElearningService;
import com.example.smile.util.DensityUtil;
import com.example.smile.util.FileUtil;
import com.example.smile.util.SmileUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import org.commonmark.node.Text;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class MoreFragment extends Fragment {

    public static UserEntity userEntity;

    TextView login;
    TextView register;
    TextView name;
    RoundedImageView image;
    LinearLayout un_login;
    LinearLayout hav_login;

    SharedPreferences sharedPreferences;

    ScrollView list;
    TextView group_manage;
    TextView change_passwd;
    TextView change_backgroud;
    TextView release_info;
    TextView check_update;
    TextView share_app;
    TextView support_us;
    TextView logout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_main, container, false);
        initView(view);

        sharedPreferences = getActivity().getSharedPreferences("more", Context.MODE_PRIVATE);
        if(sharedPreferences!=null) {
            userEntity = JSON.parseObject(sharedPreferences.getString("userEntity",""),UserEntity.class);
            if(userEntity == null || userEntity.getId()==null||userEntity.getName()==""||userEntity.getPasswd()==""){
                viewLogout();
            }
            else{
                viewLogin(userEntity.getName());
            }
        }else{
            viewLogout();
        }


        initListener();
        return view;
    }

    void viewLogin(String user_name){
        un_login.setVisibility(View.GONE);
        hav_login.setVisibility(View.VISIBLE);
        list.setVisibility(View.VISIBLE);
        name.setText(user_name);
    }

    void viewLogout(){
        un_login.setVisibility(View.VISIBLE);
        hav_login.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        image.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),R.drawable.note_user_image));
    }

    ActivityResultLauncher<Intent> selectImage;
    ActivityResultLauncher<Intent> resizeAndSetImage;
    void initView(View view){
        un_login = view.findViewById(R.id.more_un_login);
        hav_login = view.findViewById(R.id.more_hav_login);
        login = view.findViewById(R.id.more_login);
        register = view.findViewById(R.id.more_register);
        name = view.findViewById(R.id.more_user_name);
        image = view.findViewById(R.id.more_user_image);
        list = view.findViewById(R.id.more_list);
        group_manage = view.findViewById(R.id.more_group_manage);
        change_passwd = view.findViewById(R.id.more_change_passwd);
        change_backgroud = view.findViewById(R.id.more_change_backgroud);
        release_info = view.findViewById(R.id.more_release_info);
        check_update = view.findViewById(R.id.more_check_update);
        support_us = view.findViewById(R.id.more_support_me);
        share_app = view.findViewById(R.id.more_share_app);
        logout = view.findViewById(R.id.more_logout);
        selectImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                    Uri selectedImage = result.getData().getData(); //获取系统返回的照片的Uri
                    resizeImage(selectedImage);
                } else {
                    Toast.makeText(getContext(), "操作失败", Toast.LENGTH_LONG).show();
                }

            }
        });
        resizeAndSetImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getData()==null) return;
                Bitmap photo = result.getData().getParcelableExtra("data");
                image.setImageBitmap(photo);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtil.writeBitmapFile(getContext(),"user_image", photo);
                    }
                }).start();
            }
        });
    }

    void initListener(){
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().clear().apply();
                viewLogout();
                Intent intent = new Intent(GroupFragment.NIRVANA);
                MainActivity.getInstance().sendBroadcast(intent);
            }
        });

        change_backgroud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //changeBackgroud();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,null);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                selectImage.launch(intent);
            }
        });

        group_manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.getInstance(), GroupManagerActivity.class);
                intent.putExtra("userEntity",userEntity);
                startActivity(intent);
            }
        });
    }

    void sendMessage(int what, Object obj){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        myHandler.sendMessage(msg);
    }

    final int LOGIN = 0;
    final int REGISTER = 1;
    MyHandler myHandler = new MyHandler(MoreFragment.this);
    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        private WeakReference<MoreFragment> weakReference;

        public MyHandler(MoreFragment weakReference) {
            this.weakReference = new WeakReference(weakReference);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what==LOGIN){
                UserEntity userEntity = (UserEntity)msg.obj;
                if(userEntity == null) {Toast.makeText(getContext(),"登录失败", Toast.LENGTH_SHORT).show();return;}
                sharedPreferences.edit().putString("userEntity",JSON.toJSONString(userEntity)).apply();
                viewLogin(userEntity.getName());
                Intent intent = new Intent(GroupFragment.NIRVANA);
                MainActivity.getInstance().sendBroadcast(intent);
            }else if(msg.what == REGISTER){
                boolean flag = (boolean)msg.obj;
                if(!flag){
                    alert.setVisibility(View.VISIBLE);
                }
                else{
                    alert.setVisibility(View.INVISIBLE);
                    registerAlert.dismiss();
                }
            }

        }
    }

    public void login(){
        View login_view = LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.more_login, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.getInstance());
        alertDialogBuilder.setView(login_view);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        EditText pass_a = login_view.findViewById(R.id.more_login_account);
        EditText pass_e = login_view.findViewById(R.id.more_login_passwd);
        Button confirm = login_view.findViewById(R.id.more_login_confirm);
        Button cancel = login_view.findViewById(R.id.more_login_cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View view) {
                String account = pass_a.getText().toString();
                String passwd = pass_e.getText().toString();
                alertDialog.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UserEntity userEntity = SmileUtil.login(account,passwd);
                        sendMessage(LOGIN, userEntity);
                    }
                }).start();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    TextView alert;
    AlertDialog registerAlert;
    void register(){
        View login_view = LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.more_register, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.getInstance());
        alertDialogBuilder.setView(login_view);
        registerAlert = alertDialogBuilder.create();
        registerAlert.setCancelable(false);
        registerAlert.show();
        alert = login_view.findViewById(R.id.more_register_account_repeat);
        alert.setVisibility(View.INVISIBLE);
        EditText pass_n = login_view.findViewById(R.id.more_register_username);
        EditText pass_a = login_view.findViewById(R.id.more_register_account);
        EditText pass_e = login_view.findViewById(R.id.more_register_passwd);
        Button confirm = login_view.findViewById(R.id.more_register_confirm);
        Button cancel = login_view.findViewById(R.id.more_register_cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View view) {
                String name = pass_n.getText().toString();
                String account = pass_a.getText().toString();
                String passwd = pass_e.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SmileUtil.init();
                        boolean a = SmileUtil.register(name,account,passwd);
                        sendMessage(REGISTER, a);
                        if(!a) return;
                        UserEntity userEntity = SmileUtil.login(account,passwd);
                        sendMessage(LOGIN, userEntity);
                    }
                }).start();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAlert.dismiss();
            }
        });
    }
    public void resizeImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //裁剪的大小
        intent.putExtra("outputX", DensityUtil.dp2px(getContext(),50));
        intent.putExtra("outputY", DensityUtil.dp2px(getContext(),50));
        intent.putExtra("return-data", true);
        resizeAndSetImage.launch(intent);
    }
}
