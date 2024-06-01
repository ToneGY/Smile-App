package com.example.smile.activity;

import static android.widget.Toast.LENGTH_SHORT;

import static com.example.smile.fragment.GroupFragment.GROUPITEM;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smile.R;
import com.example.smile.constants.Constants;
import com.example.smile.entity.ServerTodoEntity;
import com.example.smile.entity.TodoEntity;
import com.example.smile.fragment.GroupFragment;
import com.example.smile.util.TimeUtil;
import com.github.gzuliyujiang.wheelpicker.DatimePicker;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatimePickedListener;

public class TodoContentActivity extends AppCompatActivity {
    public static final String TODO_CONTENT_ENTITY = "todo.content.entity";
    ImageView concile;
    ImageView confirm;
    TextView actionbar_text;
    EditText title;
    EditText content;
    RadioGroup radioGroup;
    TextView nice_replace;
    TextView time_select;
    TodoEntity entity;
    TodoEntity newEntity;
    ServerTodoEntity serverTodoEntity;
    boolean isTodo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_content_detail);
        initVariable();
        initData();
        initView();
        initListener();
    }

    boolean compare(TodoEntity todo1, TodoEntity todo2){
        return todo1.toString().equals(todo2.toString());
    }

    private void initListener() {
        concile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0,R.anim.activity_change);

            }
        });



        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(title.getText().toString().equals("")){
                    Toast.makeText(TodoContentActivity.this,"标题栏不能为空", LENGTH_SHORT).show();
                    return;
                }
                newEntity = new TodoEntity(entity);
                newEntity.setTitle(title.getText().toString());
                newEntity.setContent(content.getText().toString());
                newEntity.setEnd_time(time_select.getText().toString());
                if(getNice()==-1 && entity.getNice()!= Constants.NICE.OVERDUE && entity.getNice() != Constants.NICE.DONE){
                    newEntity.setNice(Constants.NICE.VERY_EASY);
                }else if(getNice() != -1){
                    newEntity.setNice(getNice());
                }
                boolean compare = compare(entity,newEntity);
                if(compare) finish();
                if(isTodo) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(TodoContentActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.MAIN_TODO, true);
                    intent.putExtra(MainActivity.MAIN_TODO_ISMODIFIED, true);
                    intent.putExtra(MainActivity.MAIN_TODO_ISADDED, false);
                    intent.putExtra(MainActivity.MAIN_TODO_ENTITY, newEntity);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.activity_change);
                }else{
                    ServerTodoEntity myServerTodoEntity = new ServerTodoEntity(serverTodoEntity.getGroup_id(), newEntity);
                    Intent intent = new Intent();
                    intent.setAction(GroupFragment.SERVERTODOENTITY);
                    intent.putExtra("isModified",true);
                    intent.putExtra("ServerTodoEntity", myServerTodoEntity);
                    sendBroadcast(intent);
                    finish();
                }

            }
        });

        if(entity.getNice()!= Constants.NICE.DONE && entity.getNice()!= Constants.NICE.OVERDUE)
            time_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatimePicker datimePicker = new DatimePicker(TodoContentActivity.this);
                    datimePicker.setTitle("选择结束时间");
                    datimePicker.setOnDatimePickedListener(new OnDatimePickedListener() {
                        @Override
                        public void onDatimePicked(int year, int month, int day, int hour, int minute, int second) {
                            @SuppressLint("DefaultLocale") String time = String.valueOf(year) + " " +
                                    String.format("%02d",month) + "-" + String.format("%02d", day) + " " +
                                    String.format("%02d",hour) + ":" + String.format("%02d", minute);
                            if(TimeUtil.greaterCompare(time, TimeUtil.getCompliteTime())){
                                time_select.setText(time);
                            }
                            else{
                                Toast.makeText(TodoContentActivity.this, "请选择位于当前时间之后的时间", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    datimePicker.show();
                }
            });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int nice = getNice();
                setBackground(title, nice);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    public int getNice() {
        int id = radioGroup.getCheckedRadioButtonId();
        switch (id){
            case R.id.todo_content_detail_nice_select_veryeasy:
                return Constants.NICE.VERY_EASY;
            case R.id.todo_content_detail_nice_select_easy:
                return Constants.NICE.EASY;
            case R.id.todo_content_detail_nice_select_important:
                return Constants.NICE.IMPORTANT;
            case R.id.todo_content_detail_nice_select_veryimportant:
                return Constants.NICE.VERY_IMPORTANT;
            default: return -1;
        }
    }

    private void initData() {
        isTodo = getIntent().getBooleanExtra("isTodo", true);
        if(isTodo) entity = getIntent().getParcelableExtra(TODO_CONTENT_ENTITY);
        else {
            serverTodoEntity = (ServerTodoEntity)getIntent().getSerializableExtra(GROUPITEM);
            entity = new TodoEntity(serverTodoEntity);
        }

    }

    private void initView() {
        title.setText(entity.getTitle());
        Typeface.Builder builder = new Typeface.Builder(getAssets(), "xingkai_piaoyi.ttf");
        title.setTypeface(builder.build());
        content.setText(entity.getContent());
        time_select.setText(entity.getEnd_time());
        int nice = entity.getNice();
        setBackground(title, nice);
        if(Constants.NICE.DONE == nice){
            nice_replace.setVisibility(View.VISIBLE);
            radioGroup.setVisibility(View.INVISIBLE);
            nice_replace.setText("已完成");
        }else if(Constants.NICE.OVERDUE == nice){
            nice_replace.setVisibility(View.VISIBLE);
            radioGroup.setVisibility(View.INVISIBLE);
            nice_replace.setText("已逾期");
        }
        else setNiceSelector(radioGroup, nice);
    }

    private void setNiceSelector(RadioGroup view, int nice) {
        switch (nice){
            case Constants.NICE.VERY_IMPORTANT:
                view.check(R.id.todo_content_detail_nice_select_veryimportant);
                break;
            case Constants.NICE.IMPORTANT:
                view.check(R.id.todo_content_detail_nice_select_important);
                break;
            case Constants.NICE.EASY:
                view.check(R.id.todo_content_detail_nice_select_easy);
                break;
            case Constants.NICE.VERY_EASY:
                view.check(R.id.todo_content_detail_nice_select_veryeasy);
                break;
            default:
                view.clearCheck();
        }
    }

    public void setBackground(EditText view, int nice){
        switch (nice){
            case Constants.NICE.VERY_IMPORTANT:
                view.setBackgroundResource(R.drawable.content_border_veryimportant);
                break;
            case Constants.NICE.IMPORTANT:
                view.setBackgroundResource(R.drawable.content_border_important);
                break;
            case Constants.NICE.EASY:
                view.setBackgroundResource(R.drawable.content_border_easy);
                break;
            case Constants.NICE.VERY_EASY:
                view.setBackgroundResource(R.drawable.content_border_veryeasy);
                break;
            default:
                view.setBackgroundResource(R.drawable.content_border_done);
        }
    }

    private void initVariable() {
            concile = findViewById(R.id.content_detail_concile);
            confirm = findViewById(R.id.content_detail_confirm);
            actionbar_text = findViewById(R.id.todo_content_detail_actionbar_text);
            title = findViewById(R.id.todo_content_detail_title);
            content = findViewById(R.id.todo_content_detail_content);
            radioGroup = findViewById(R.id.todo_content_detail_nice_select);
            time_select = findViewById(R.id.todo_content_time_select);
            nice_replace = findViewById(R.id.todo_content_detail_replace);
    }
}
