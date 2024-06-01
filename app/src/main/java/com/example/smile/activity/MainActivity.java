package com.example.smile.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;

import com.example.smile.fragment.NoteFragment;
import com.example.smile.interfaceclass.DeleteMessage;
import com.example.smile.R;
import com.example.smile.dao.TodoDao;
import com.example.smile.entity.TodoEntity;
import com.example.smile.fragment.MoreFragment;
import com.example.smile.fragment.GroupFragment;
import com.example.smile.fragment.TodoListFragment;
import com.example.smile.service.ElearningLoginService;
import com.example.smile.service.TodoAlarmService;
import com.example.smile.util.TimeUtil;

public class MainActivity extends AppCompatActivity {

    TodoListFragment todoListFragment;
    GroupFragment groupFragment;
    NoteFragment noteFragment;
    MoreFragment moreFragment;
    TodoDao todoDao;
    RadioGroup radioGroup;


    DeleteMessage deleteMessage;

    public static final String MAIN_TODO = "mian.todo";
    public static final String MAIN_TODO_ISMODIFIED = "main.todo.ismodified";
    public static final String MAIN_TODO_ISADDED = "main.todo.isadded";
    public static final String MAIN_TODO_ENTITY = "main.todo.entity";
    Fragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);


        initService();


        initVariable();

        //初始化数据，对intent作出反应
        initData();

        initView();

        initListener();
        initBroadCastReceiver();

    }


    private static MainActivity instance;


    public static MainActivity getInstance() {
        return instance;
    }

    void initBroadCastReceiver(){

        BroadcastReceiver bcr = noteFragment.getBoadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NoteFragment.NOTE_BROADCAST_FLAG);
        registerReceiver(bcr, intentFilter);
    }

    private void initService() {
        Intent intentservice = new Intent(this, TodoAlarmService.class);
        startForegroundService(intentservice);


        //elearning
       Intent elearningLoginService = new Intent(this, ElearningLoginService.class);
        startService(elearningLoginService);
    }

    private void initView() {
        radioGroup.setAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_up_in));

    }

    private void initListener() {

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.slower_sliderbar_todo)
                    switchFragment(todoListFragment);
                else if(i == R.id.slower_sliderbar_schedule)
                    switchFragment(groupFragment);
                else if(i == R.id.slower_sliderbar_note)
                    switchFragment(noteFragment);
                else if(i == R.id.slower_sliderbar_more)
                    switchFragment(moreFragment);
            }
        });
    }

    private void initVariable(){
        radioGroup = findViewById(R.id.slower_sliderbar);

        todoListFragment = new TodoListFragment();
        groupFragment = new GroupFragment();
        noteFragment = new NoteFragment();
        moreFragment = new MoreFragment();
        todoDao = new TodoDao(this);
    }

    private void initData(){
        radioGroup.check(R.id.slower_sliderbar_todo);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_fragment_container, noteFragment).hide(noteFragment).commit();

        Intent intent = getIntent();
        if(intent.getBooleanExtra(MAIN_TODO,false)){
            if(intent.getBooleanExtra(MAIN_TODO_ISMODIFIED,false)){
                TodoEntity entity = intent.getParcelableExtra(MAIN_TODO_ENTITY);
                todoDao.update(entity);
            }
            else if(intent.getBooleanExtra(MAIN_TODO_ISADDED, false)){
                TodoEntity entity = intent.getParcelableExtra(MAIN_TODO_ENTITY);
                todoDao.create(entity);
                entity.setId(todoDao.getNewId());

                //向TodoAlarmService 发送广播
                Intent intent1 = new Intent(TodoAlarmService.TODO_ALARM_SERVICE_RECEIVE_ENTITY);
                intent1.putExtra("TodoEntity", entity);
                intent1.putExtra("alarm_time", (Long)TimeUtil.getTimeStamp(entity.getEnd_time())-1000*60*30);
                sendBroadcast(intent1);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, todoListFragment).commit();
            currentFragment = todoListFragment;
        }
        else{
            getSupportFragmentManager().beginTransaction() .replace(R.id.main_fragment_container, todoListFragment).commit();
            currentFragment = todoListFragment;
        }

        deleteMessage = new DeleteMessage() {
            @Override
            public void inDeleteMode() {
                radioGroup.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_down_out));
                radioGroup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void outDeletMode() {
                radioGroup.setVisibility(View.VISIBLE);
                radioGroup.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up_in));

            }
        };
        todoListFragment.setDeleteMessage(deleteMessage);
        groupFragment.setDeleteMessage(deleteMessage);

    }

    private void switchFragment(Fragment targetFragment) {
        //已经显示就不切换
        if(currentFragment == targetFragment)
            return;

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        //没有添加则先完成添加再显示
        if (!targetFragment.isAdded()) {
            transaction
                    .hide(currentFragment)
                    .add(R.id.main_fragment_container, targetFragment)
                    .commit();

        } else {
            transaction
                    .hide(currentFragment)
                    .show(targetFragment)
                    .commit();
        }
        currentFragment = targetFragment;
    }

}