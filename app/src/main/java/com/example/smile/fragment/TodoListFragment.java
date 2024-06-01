package com.example.smile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smile.activity.LineChartActivity;
import com.example.smile.interfaceclass.DeleteMessage;
import com.example.smile.R;
import com.example.smile.activity.TodoAddContentActivity;
import com.example.smile.activity.TodoContentActivity;
import com.example.smile.adapter.TodoContentRecycleAdapter;
import com.example.smile.dao.TodoDao;
import com.example.smile.entity.TodoEntity;

import java.util.List;
import java.util.TimeZone;

public class TodoListFragment extends Fragment {
    TextClock todo_year;
    TextClock todo_date;
    TextClock todo_time;
    ImageView todoChange;
    ImageView todoAdd;
    ImageView todoDelete;

    TodoContentRecycleAdapter adapter;
    RecyclerView recyclerView;
    TodoDao todoDao;

    public DeleteMessage getDeleteMessage() {
        return deleteMessage;
    }

    public void setDeleteMessage(DeleteMessage deleteMessage) {
        this.deleteMessage = deleteMessage;
    }

    DeleteMessage deleteMessage;
    @Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
//        Slide slide = new Slide();
//        if(enter) return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_out);
        return super.onCreateAnimation(transit, enter, nextAnim);
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.todo_main, container, false);
        initVariable(view);
        initView();
        initData();
        initListener();
        return view;
    }

    private void initListener() {
        todoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), TodoAddContentActivity.class);
                startActivity(intent);
            }
        });

        TodoContentRecycleAdapter.OnItemClickListener onItemClickListener = new TodoContentRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TodoEntity entity = adapter.getDataBases().get(position);
                Intent intent = new Intent();
                intent.setClass(getActivity(), TodoContentActivity.class);
                intent.putExtra(TodoContentActivity.TODO_CONTENT_ENTITY, entity);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if(!adapter.getIsDeleteMode()){
                    todoAdd.setVisibility(View.INVISIBLE);
                    todoChange.setVisibility(View.INVISIBLE);
                    todoDelete.setVisibility(View.VISIBLE);
                    deleteMessage.inDeleteMode();
                    adapter.setDeleteMode(true);
                }
            }
        };

        todoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> list = adapter.getSelectId();
                todoDao.delete(list);
                todoAdd.setVisibility(View.VISIBLE);
                todoChange.setVisibility(View.VISIBLE);
                todoDelete.setVisibility(View.INVISIBLE);
                adapter.setDeleteMode(false);
                adapter.setDataBases(todoDao.findAll());
                deleteMessage.outDeletMode();
            }
        });

        adapter.setOnItemClickListener(onItemClickListener);

        todoChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), LineChartActivity.class);
                startActivity(intent);
            }
        });
    }



    private void initView() {

        todo_date.setTextSize(25);
        todo_year.setTextSize(15);
        todo_time.setTextSize(15);
    }

    private void initData() {
        todo_year.setTimeZone(TimeZone.getDefault().getDisplayName(true,TimeZone.SHORT));

        todo_year.setFormat24Hour("yyyy年");
        todo_date.setFormat24Hour("MM月dd日");
        todo_time.setFormat24Hour("HH:mm");


        todo_year.setFormat12Hour("yyyy年");
        todo_date.setFormat12Hour("MM月dd日");
        todo_time.setFormat12Hour("HH:mm");

        todoDao = new TodoDao(getContext());
        adapter = new TodoContentRecycleAdapter(getContext());
        adapter.setDataBases(todoDao.findAll());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void initVariable(View view) {
        todo_year = view.findViewById(R.id.note_actionbar_year);
        todo_date = view.findViewById(R.id.note_actionbar_date);
        todo_time = view.findViewById(R.id.note_actionbar_time);
        todoChange = view.findViewById(R.id.todo_actionbar_change);
        todoAdd = view.findViewById(R.id.todo_actionbar_add);
        todoDelete = view.findViewById(R.id.todo_actionbar_delete);
        recyclerView = view.findViewById(R.id.todo_main_recycler);
    }


}
