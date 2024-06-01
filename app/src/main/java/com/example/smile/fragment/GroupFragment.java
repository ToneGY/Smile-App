package com.example.smile.fragment;

import static com.example.smile.R.drawable.content_border_done;
import static com.example.smile.R.drawable.content_border_easy;
import static com.example.smile.R.drawable.content_border_important;
import static com.example.smile.R.drawable.content_border_veryeasy;
import static com.example.smile.R.drawable.content_border_veryimportant;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.fastjson2.JSON;
import com.example.smile.R;
import com.example.smile.activity.LineChartActivity;
import com.example.smile.activity.MainActivity;
import com.example.smile.activity.TodoAddContentActivity;
import com.example.smile.activity.TodoContentActivity;
import com.example.smile.adapter.TodoContentRecycleAdapter;
import com.example.smile.constants.Constants;
import com.example.smile.entity.GroupEntity;
import com.example.smile.entity.ServerTodoEntity;
import com.example.smile.entity.TodoEntity;
import com.example.smile.entity.UserEntity;
import com.example.smile.interfaceclass.DeleteMessage;
import com.example.smile.util.SmileUtil;
import com.example.smile.util.TimeUtil;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {
    SharedPreferences sharedPreferences;
    UserEntity userEntity;
    List<GroupEntity> groupEntities;
    List<UserEntity> userEntities;
    List<ServerTodoEntity> todoEntities;

    RelativeLayout un_login;
    TextView group_sel_name;
    TextView group_sel_id;
    ImageView change_group;
    ImageView add_group;
    ImageView delete_group;
    RelativeLayout expandable_button;
    ExpandableLayout expandableLayout;

    ImageView arrow;

    RecyclerView memberRecycler;
    RecyclerView itemRecycler;
    MemeberAdapter memeberAdapter;
    ItemAdpater itemAdpater;

    private RotateAnimation mFlipAnimation;         // 下拉动画
    private RotateAnimation mReverseFlipAnimation;     // 恢复动画

    GroupEntity group_sel;

    DeleteMessage deleteMessage;
    public DeleteMessage getDeleteMessage() {
        return deleteMessage;
    }
    public void setDeleteMessage(DeleteMessage deleteMessage) {this.deleteMessage = deleteMessage;}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static String SERVERTODOENTITY = "server.todo.entity";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_fra_main, container, false);
        sharedPreferences = MainActivity.getInstance().getSharedPreferences("more", Context.MODE_PRIVATE);
        un_login = view.findViewById(R.id.group_frag_un_login);
        checkLogin();
        initView(view);
        expandableLayout.setExpanded(true);
        initListener();
        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SERVERTODOENTITY);
        MainActivity.getInstance().registerReceiver(myBroadcastReceiver,intentFilter);

        ABroadCastReceiver aBroadCastReceiver = new ABroadCastReceiver();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(NIRVANA);
        MainActivity.getInstance().registerReceiver(aBroadCastReceiver,intentFilter1);
        return view;
    }




    void checkLogin(){
        if(sharedPreferences!=null) {
            userEntity = JSON.parseObject(sharedPreferences.getString("userEntity",""), UserEntity.class);
            if(userEntity == null || userEntity.getId()==null||userEntity.getName()==""||userEntity.getPasswd()==""){
                un_login.setVisibility(View.VISIBLE);
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        groupEntities = SmileUtil.getGroupByUser(userEntity.getAccount());
                        if(groupEntities!=null && groupEntities.size()>0) {
                            userEntities = SmileUtil.getUserByGroupId(groupEntities.get(0).getId());
                            todoEntities = SmileUtil.getTodoByGroupId(groupEntities.get(0).getId());
                            sendMessage(INIT, null);
                        }
                    }
                }).start();
            }
        }else{
            un_login.setVisibility(View.VISIBLE);
        }
    }

    public static String NIRVANA= "group.entity.nirvana";
    class ABroadCastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            checkLogin();
        }
    }

    final int INIT = 0;
    final int NEW = 1;
    final int CHANGE_GROUP = 2;
    final int DELETE = 3;
    MyHandler myHandler = new MyHandler(GroupFragment.this);
    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        private final WeakReference<GroupFragment> weakReference;

        public MyHandler(GroupFragment weakReference) {
            this.weakReference = new WeakReference(weakReference);
        }
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == INIT){
                un_login.setVisibility(View.GONE);
                group_sel_name.setText(groupEntities.get(0).getName());
                group_sel_id.setText("id: "+String.valueOf(groupEntities.get(0).getId()));
                group_sel = groupEntities.get(0);
                memeberAdapter.setItems(userEntities);
                itemAdpater.setItems(todoEntities);
            }else if(msg.what == NEW){
                todoEntities = (List<ServerTodoEntity>) castList(msg.obj, ServerTodoEntity.class);
                itemAdpater.setItems(todoEntities);
            }else if(msg.what == CHANGE_GROUP){
                GroupEntity groupEntity = (GroupEntity) msg.obj;
                group_sel_name.setText(groupEntity.getName());
                group_sel_id.setText("id: "+String.valueOf(groupEntity.getId()));
                group_sel = groupEntity;
                memeberAdapter.setItems(userEntities);
                itemAdpater.setItems(todoEntities);

            }else if(msg.what == DELETE){
                todoEntities = (List<ServerTodoEntity>) castList(msg.obj, ServerTodoEntity.class);
                itemAdpater.setItems(todoEntities);
                itemAdpater.setDeleteMode(false);
            }
        }
    }
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



    class MemeberAdapter extends RecyclerView.Adapter<MemeberAdapter.ViewHolder>{
        List<UserEntity> userEntities;

        @SuppressLint("NotifyDataSetChanged")
        void setItems(List<UserEntity> userEntities){
            this.userEntities = userEntities;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.group_fra_member_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserEntity userEntity = userEntities.get(position);
            holder.account.setText("id: "+ userEntity.getAccount());
            holder.name.setText(userEntity.getName());
        }

        @Override
        public int getItemCount() {
            return userEntities == null ? 0 : userEntities.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView name;
            TextView account;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.group_fra_member_item_name);
                account = itemView.findViewById(R.id.group_fra_member_item_account);
            }
        }
    }



    public static String GROUPITEM="group.item";
    class ItemAdpater extends RecyclerView.Adapter<ItemAdpater.ViewHolder>{
        List<ServerTodoEntity> todoEntities;
        List<Integer> selectId = new ArrayList<>();
        private boolean isDeleteMode = false;
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater =LayoutInflater.from(parent.getContext());
            View itemView =inflater.inflate(R.layout.todo_content_item, parent, false);
            return new ViewHolder(itemView);
        }
        ServerTodoEntity entity = new ServerTodoEntity();
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

            if(position < getItemCount()-1) {
                entity= todoEntities.get(position);
                setBackgroudByNice(holder.item, entity.getNice());
            }
            if(position == getItemCount()-1) {
                holder.getItemView().setMinimumHeight(MainActivity.getInstance().getResources().getDimensionPixelOffset(R.dimen.todo_list_item_end_height));
                holder.getItemView().setVisibility(View.INVISIBLE);
            }else if(Constants.NICE.NULLBLOCK != entity.getNice()){
                holder.begin_time.setText(TimeUtil.getDateAndTime(entity.getBegin_time()));
                holder.end_time.setText(TimeUtil.getDateAndTime(entity.getEnd_time()));
                holder.text.setText(entity.getTitle());
                holder.getItemView().setVisibility(View.VISIBLE);
                holder.getItemView().setMinimumHeight(MainActivity.getInstance().getResources().getDimensionPixelOffset(R.dimen.todo_list_item_height));
            }

            if (!isDeleteMode) {
                holder.delete.setVisibility(View.INVISIBLE);
            } else{
                if(position < getItemCount()-1){
                    holder.delete.setVisibility(View.VISIBLE);
                    if (selectId.contains(entity.getId())) holder.de_sel.setVisibility(View.VISIBLE);
                    else holder.de_sel.setVisibility(View.INVISIBLE);
                }else{
                    holder.delete.setVisibility(View.INVISIBLE);
                }
            }

            ServerTodoEntity finalEntity = entity;
            holder.getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isDeleteMode) {
                        ServerTodoEntity entity = todoEntities.get(position);
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.getInstance(), TodoContentActivity.class);
                        intent.putExtra("isTodo",false);
                        intent.putExtra(GROUPITEM, entity);
                        startActivity(intent);
                    }
                    else{
                        if(!selectId.contains(finalEntity.getId())){
                            selectId.add(finalEntity.getId());
                            holder.de_sel.setVisibility(View.VISIBLE);
                        } else{
                            selectId.remove(finalEntity.getId());
                            holder.de_sel.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });

            holder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(!isDeleteMode) {
                        add_group.setVisibility(View.INVISIBLE);
                        change_group.setVisibility(View.INVISIBLE);
                        delete_group.setVisibility(View.VISIBLE);
                        deleteMessage.inDeleteMode();
                        setDeleteMode(true);
                        selectId.add(entity.getId());
                        holder.de_sel.setVisibility(View.VISIBLE);
                    }
                    return false;
                }
            });
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setDeleteMode(boolean isDeleteMode){
            //每次设置删除模式时，清除保存的id
            selectId.clear();
            this.isDeleteMode = isDeleteMode;
            //通知适配器数据改变，重新渲染
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return todoEntities == null ? 0 : todoEntities.size()+1;
        }
        public boolean getIsDeleteMode(){
            return isDeleteMode;
        }

        public List<Integer> getSelectId() {
            return selectId;
        }
        @SuppressLint("NotifyDataSetChanged")
        public void setItems(List<ServerTodoEntity> serverTodoEntities){
            todoEntities = serverTodoEntities;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            RelativeLayout item;
            TextView begin_time;
            TextView end_time;
            TextView text;
            RelativeLayout delete;
            ImageView de_sel;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.todo_content_item_init);
                begin_time = itemView.findViewById(R.id.content_item_todo_begintime);
                end_time = itemView.findViewById(R.id.content_item_todo_endtime);
                text = itemView.findViewById(R.id.content_item_text);
                delete = itemView.findViewById(R.id.todo_item_delete_button);
                de_sel = itemView.findViewById(R.id.todo_content_item_selected);
            }

            public View getItemView(){
                return itemView;
            }
        }
    }

    public static void setBackgroudByNice(RelativeLayout item, int nice){
        if(Constants.NICE.VERY_IMPORTANT == nice) item.setBackgroundResource(content_border_veryimportant);
        else if(Constants.NICE.IMPORTANT == nice) item.setBackgroundResource(content_border_important);
        else if(Constants.NICE.EASY == nice) item.setBackgroundResource(content_border_easy);
        else if(Constants.NICE.VERY_EASY == nice) item.setBackgroundResource(content_border_veryeasy);
        else item.setBackgroundResource(content_border_done);
    }

    void initView(View view){
        memberRecycler = view.findViewById(R.id.group_fra_member_recycler);
        itemRecycler = view.findViewById(R.id.group_fra_item_recycler);
        change_group = view.findViewById(R.id.group_fra_actionbar_change);
        group_sel_id = view.findViewById(R.id.group_frag_cur_id);
        group_sel_name = view.findViewById(R.id.group_frag_cur_name);
        add_group = view.findViewById(R.id.group_fra_actionbar_add);
        delete_group = view.findViewById(R.id.group_fra_actionbar_delete);
        expandable_button = view.findViewById(R.id.group_fra_member_expandable_button);
        expandableLayout = view.findViewById(R.id.group_fra_expandable);
        memberRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
        itemRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
        memeberAdapter = new MemeberAdapter();
        itemAdpater = new ItemAdpater();
        memberRecycler.setAdapter(memeberAdapter);
        itemRecycler.setAdapter(itemAdpater);
        arrow = view.findViewById(R.id.group_fra_memeber_image);
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250); // 设置持续时间
        mFlipAnimation.setFillAfter(true); // 动画执行完是否停留在执行完的状态
        mReverseFlipAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);
    }

    void initListener(){
        add_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("ServerTodoEntity", group_sel.getId());
                intent.setClass(getActivity(), TodoAddContentActivity.class);
                startActivity(intent);
            }
        });

        delete_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> list = itemAdpater.getSelectId();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("sad",list.toString());
                        SmileUtil.deleteTodoEntities(list);
                        List<ServerTodoEntity> todoEntities = SmileUtil.getTodoByGroupId(group_sel.getId());
                        sendMessage(DELETE, todoEntities);
                    }
                }).start();
                add_group.setVisibility(View.VISIBLE);
                change_group.setVisibility(View.VISIBLE);
                delete_group.setVisibility(View.INVISIBLE);
                deleteMessage.outDeletMode();
            }
        });

        change_group.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                View alertView = (View)LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.group_select_group,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
                AlertDialog alertDialog = builder.setView(alertView).create();
                TextView curId = alertView.findViewById(R.id.group_curID);
                curId.setText("id: " + String.valueOf(group_sel.getId()));
                RecyclerView recyclerView = alertView.findViewById(R.id.group_select_recycle);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
                class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder>{
                    List<GroupEntity> groupEntities;

                    @SuppressLint("NotifyDataSetChanged")
                    void setItems(List<GroupEntity> groupEntities){
                        this.groupEntities = groupEntities;
                        notifyDataSetChanged();
                    }

                    @NonNull
                    @Override
                    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new ViewHolder(LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.group_fra_member_item, parent, false));
                    }

                    @Override
                    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                        GroupEntity groupEntitiy = groupEntities.get(position);
                        holder.account.setText("id: "+ groupEntitiy.getId());
                        holder.name.setText(groupEntitiy.getName());
                        holder.getItemView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        userEntities = SmileUtil.getUserByGroupId(groupEntitiy.getId());
                                        todoEntities = SmileUtil.getTodoByGroupId(groupEntitiy.getId());
                                        sendMessage(CHANGE_GROUP, groupEntitiy);
                                    }
                                }).start();
                                alertDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public int getItemCount() {
                        return groupEntities == null ? 0 : groupEntities.size();
                    }

                    class ViewHolder extends RecyclerView.ViewHolder{
                        TextView name;
                        TextView account;
                        public ViewHolder(@NonNull View itemView) {
                            super(itemView);
                            name = itemView.findViewById(R.id.group_fra_member_item_name);
                            account = itemView.findViewById(R.id.group_fra_member_item_account);
                        }
                        public View getItemView(){
                            return itemView;
                        }
                    }
                }
                GroupAdapter groupAdapter = new GroupAdapter();
                recyclerView.setAdapter(groupAdapter);
                groupAdapter.setItems(groupEntities);
                alertDialog.show();
            }
        });

        expandable_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(expandableLayout.isExpanded()){
                    expandableLayout.collapse();
                    arrow.startAnimation(mFlipAnimation);
                }else{
                    expandableLayout.expand();
                    arrow.startAnimation(mReverseFlipAnimation);
                }
            }
        });
    }

    class MyBroadcastReceiver extends android.content.BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isModified = intent.getBooleanExtra("isModified",false);
            ServerTodoEntity serverTodoEntity = (ServerTodoEntity)intent.getSerializableExtra("ServerTodoEntity");
            if(!isModified) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SmileUtil.addTodoEntity(serverTodoEntity);
                        todoEntities = SmileUtil.getTodoByGroupId(serverTodoEntity.getGroup_id());
                        sendMessage(NEW, todoEntities);
                    }
                }).start();
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SmileUtil.updateTodoEntity(serverTodoEntity);
                        todoEntities = SmileUtil.getTodoByGroupId(serverTodoEntity.getGroup_id());
                        sendMessage(NEW, todoEntities);
                    }
                }).start();
            }
        }
    }
}
