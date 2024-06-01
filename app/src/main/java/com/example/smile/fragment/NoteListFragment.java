package com.example.smile.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smile.R;
import com.example.smile.activity.MainActivity;
import com.example.smile.activity.NoteHWDetailActivity;
import com.example.smile.adapter.AnimatedExpandableListView;
import com.example.smile.adapter.expandableLayout.ExpandableLayout;
import com.example.smile.entity.Elearning.AssignmentEntity;
import com.example.smile.entity.Elearning.CourseEntity;
import com.example.smile.entity.Elearning.HWDetailEntity;
import com.example.smile.entity.Elearning.SerializableHashMap;
import com.example.smile.service.ElearningService;
import com.example.smile.util.DensityUtil;
import com.example.smile.util.ElearningUtil;
import com.example.smile.view.CustomPopWindow;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Circle;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NoteListFragment extends Fragment {
    private AnimatedExpandableListView listView;
    public ExampleAdapter adapter;
    private List<CourseEntity> items = new ArrayList<>();
    private HashMap<Integer, List<AssignmentEntity>> child_items = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void createAdapter(){
        if(adapter==null && MainActivity.getInstance()!=null)
            adapter = new ExampleAdapter(MainActivity.getInstance());
    }
    SharedPreferences e_load;
    AlertDialog loading;
    public void load(){
        View login_view = LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.eleanring_login, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.getInstance());
        alertDialogBuilder.setView(login_view);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        EditText user_e = login_view.findViewById(R.id.note_username);
        EditText pass_e = login_view.findViewById(R.id.note_passwd);
        Button confirm = login_view.findViewById(R.id.note_login_confirm);
        Button cancel = login_view.findViewById(R.id.note_login_cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View view) {
                String user_name = user_e.getText().toString();
                String passwd = pass_e.getText().toString();
                alertDialog.dismiss();
                e_load.edit().putString("user_name", user_name).apply();
                e_load.edit().putString("passwd",passwd).apply();
                Intent intent = new Intent(MainActivity.getInstance(), ElearningService.class);
                requireActivity().startService(intent);
                View progressBar = LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.loading, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.getInstance(),R.style.TransparentDialog);
                alert.setView(progressBar);
                loading = alert.create();
                loading.show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("debug","NoteListFragment_onCreateView");
        View view = inflater.inflate(R.layout.note_hw_fragment, container, false);
        createAdapter();
        listView = (AnimatedExpandableListView) view.findViewById(R.id.note_list);
        listView.setOnRefreshListener(new AnimatedExpandableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent elearningService = new Intent(MainActivity.getInstance(), ElearningService.class);
                MainActivity.getInstance().startService(elearningService);
            }
        });
        adapter.setItems(items,child_items);
        if(adapter == null){
            Log.e("NoteListFragment","adapter is null");
        }
        Log.e("NoteList","setAdapter");
        listView.setAdapter(adapter);
        Log.e("Note_List","setAdapter");

        e_load = MainActivity.getInstance().getSharedPreferences("elearning_load", MODE_PRIVATE);
        String user_name =  e_load.getString("user_name",null);
        String passwd = e_load.getString("passwd",null);
        if(user_name == null || user_name == "" || passwd == null || passwd == "") {
            load();
        }
        // In order to show animations, we need to use a custom click handler
        // for our ExpandableListView.
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and expandGroupWithAnimation(int) to animate group expansion/collapse.
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                }
                return true;
            }
        });

        return view;
    }



    private static class ChildHolder {
        TextView title;
        TextView hint;
        TextView content;
        ExpandableLayout expandableLayout;
        TextView begin_time;
        TextView end_time;
        Button button;
    }

    private static class GroupHolder {
        TextView title;
    }

    public void setItems(List<CourseEntity> items, HashMap<Integer, List<AssignmentEntity>> child_items, boolean fromWeb){
        this.items = items;
        this.child_items = child_items;
        if(adapter!= null) adapter.setItems(items,child_items);
        if(fromWeb){
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
            String date = format.format(new Date());
            // Call onRefreshComplete when the list has been refreshed.
            listView.onRefreshComplete(date);
        }
    }

    private class ExampleAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
        private LayoutInflater inflater;

        private List<CourseEntity> items;
        private HashMap<Integer, List<AssignmentEntity>> child_items;
        private HashMap<Integer,HashSet<Integer>> mExpandedPositionSet = new HashMap<>();



        public ExampleAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setItems(List<CourseEntity> items, HashMap<Integer, List<AssignmentEntity>> child_items) {
            if(items!=null && loading!= null && loading.isShowing()) loading.dismiss();
            this.items = items;
            this.child_items = child_items;
            mExpandedPositionSet = new HashMap<>();
            notifyDataSetChanged();
        }


        @Override
        public AssignmentEntity getChild(int groupPosition, int childPosition) {
            return child_items.get(items.get(groupPosition).getId()).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            AssignmentEntity item = getChild(groupPosition, childPosition);

            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.note_main_list_child, parent, false);
                holder.expandableLayout = convertView.findViewById(R.id.note_expand_item);
                holder.title = (TextView) convertView.findViewById(R.id.note_child);
                holder.hint = (TextView) convertView.findViewById(R.id.note_state);
                holder.content = (TextView) convertView.findViewById(R.id.enpand_text);
                holder.button = (Button) convertView.findViewById(R.id.note_item_button);
                holder.begin_time = (TextView) convertView.findViewById(R.id.note_item_begintime);
                holder.end_time = (TextView) convertView.findViewById(R.id.note_item_endtime);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }

            holder.title.setText(item.getTitle());
            if(!item.isSubmitted()) holder.hint.setVisibility(View.VISIBLE);
            else holder.hint.setVisibility(View.INVISIBLE);
            if(item.getSubmission_types().contains("online_upload"))
                holder.content.setText(item.isSubmitted() ? "线上提交 已提交" : "线上提交 未提交");
            else holder.content.setText("线下或其他形式");

            holder.begin_time.setText(item.getDate_cre() == null ? "" : item.getDate_cre().replace("T"," ").replace("Z",""));
            holder.end_time.setText(item.getDate_due() == null ? "" : item.getDate_due().replace("T"," ").replace("Z",""));


            if(mExpandedPositionSet.get(item.getCourse_id()) == null) {
                HashSet<Integer> hs = new HashSet<>();
                mExpandedPositionSet.put(item.getCourse_id(), hs);
            }
            if(holder.expandableLayout !=null) {
                ExpandableLayout finalExpandableLayout = holder.expandableLayout;
                holder.expandableLayout.setOnExpandListener(new ExpandableLayout.OnExpandListener() {
                    @Override
                    public void onExpand(boolean expanded) {
                        registerExpand(item.getCourse_id(), item.getId());
                    }
                });
                finalExpandableLayout.setExpand(mExpandedPositionSet.get(item.getCourse_id()).contains(item.getId()));
            }

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getContext(), NoteHWDetailActivity.class);
                            intent.putExtra("url", item.getDetail_url());
                            startActivity(intent);

                        }
                    }).start();

                }
            });

            return convertView;
        }

        private void registerExpand(int groupPosition, int childPosition) {
            if (mExpandedPositionSet.get(groupPosition).contains(childPosition)) {
                removeExpand(groupPosition, childPosition);
            }else {
                addExpand(groupPosition, childPosition);
            }
        }

        private void removeExpand(int groupPosition, int childPosition) {
            mExpandedPositionSet.get(groupPosition).remove(childPosition);
        }

        private void addExpand(int groupPosition, int childPosition) {
            mExpandedPositionSet.get(groupPosition).add(childPosition);
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            if(groupPosition == getGroupCount()-1) return 0;
            return child_items.get(items.get(groupPosition).getId()).size();
        }

        @Override
        public CourseEntity getGroup(int groupPosition) {
            if(groupPosition == getGroupCount()-1) return null;
            return items.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return items == null ? 0 : items.size() + 1;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            CourseEntity item = getGroup(groupPosition);
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.note_main_list_header, parent, false);
                if(groupPosition == getGroupCount()-1) {
                    convertView.setBackgroundResource(R.drawable.note_hw_bottom_item);
                }
                else{
                    convertView.setBackgroundColor(0);
                }
                holder.title = (TextView) convertView.findViewById(R.id.note_header);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }

            if(groupPosition == getGroupCount()-1){
                AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView.getLayoutParams();
                params.height = DensityUtil.dp2px(MainActivity.getInstance(),100);
                convertView.setLayoutParams(params);
                holder.title.setText("");
            }else{
                holder.title.setText(item.getShortName().substring(item.getShortName().indexOf(" ")+1).replaceFirst(" ","\n"));
            }
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

    }
}
