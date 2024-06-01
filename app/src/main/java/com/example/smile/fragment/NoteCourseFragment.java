package com.example.smile.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smile.R;
import com.example.smile.activity.CourseActivity;
import com.example.smile.adapter.TodoContentRecycleAdapter;
import com.example.smile.entity.Elearning.CourseEntity;
import com.example.smile.util.ElearningUtil;

import java.util.List;
import java.util.Objects;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

public class NoteCourseFragment extends Fragment {

    GridViewAdapter gridViewAdapter;
    GridViewWithHeaderAndFooter gridView;
    List<CourseEntity> lce;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateAdapter();
    }

    private void CreateAdapter() {
        if(getContext() == null) return;
        if(gridViewAdapter == null) gridViewAdapter = new GridViewAdapter(getContext(),lce);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("debug","NoteCourseFragment_onCreateView");
        View view = inflater.inflate(R.layout.note_course_fragment, container,false);
        gridView = view.findViewById(R.id.course_gridView);
        View footerView = inflater.inflate(R.layout.course_bottom_bar, null);
        gridView.addFooterView(footerView);
        CreateAdapter();
        if(gridViewAdapter == null) Log.e("debug","gridviewAdapter is null");
        gridView.setAdapter(gridViewAdapter);
        return view;
    }


    public void setItems(List<CourseEntity> items){
        lce = items;
        CreateAdapter();
        if(gridViewAdapter != null) gridViewAdapter.setItems(lce);
    }

    private class GridViewAdapter extends BaseAdapter{
        private List<CourseEntity> items;
        private LayoutInflater mInflater;
        public GridViewAdapter(Context context, List<CourseEntity> data) {
            this.items = data;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        public void setItems(List<CourseEntity> items){
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(items == null || items.size() == 0) return 0;
            if (Objects.equals(items.get(items.size() - 1).getShortName(), "")) {
                return items.size()-1;
            }
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
        int[] colors ={R.color.molandi_s1, R.color.molandi_s2, R.color.molandi_s3,R.color.molandi_s4,R.color.molandi_s5,
                R.color.molandi_s6,R.color.molandi_s7,R.color.molandi_s8,R.color.molandi_s9,R.color.molandi_s10,
                R.color.molandi_s11,R.color.molandi_s12,R.color.molandi_s13,R.color.molandi_s14,R.color.molandi_s15,
                R.color.molandi_s16,R.color.molandi_s17,R.color.molandi_s18,R.color.molandi_s19,R.color.molandi_s20};
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CourseEntity courseEntity = items.get(i);
            View v;
            if(view == null) {
                v = mInflater.inflate(R.layout.note_course_item, viewGroup, false);
            }else{
                v = view;
            }

            ImageView iv = v.findViewById(R.id.course_color);
            TextView tv = v.findViewById(R.id.course_name);
            iv.setBackgroundColor(ContextCompat.getColor(requireContext(),colors[courseEntity.getId()%20]));//colors[courseEntity.getId()%20]
            tv.setTextColor(ContextCompat.getColor(requireContext(),colors[courseEntity.getId()%20]));
            tv.setText(courseEntity.getShortName().substring(courseEntity.getShortName().indexOf(" ")+1));
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), CourseActivity.class);
                    intent.putExtra("course",courseEntity);
                    startActivity(intent);
                }
            });


            return v;
        }

        class MyHolder extends RecyclerView.ViewHolder{
            ImageView iv;
            TextView course;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                iv = itemView.findViewById(R.id.course_color);
                course = itemView.findViewById(R.id.course_name);
            }

            public View getItemView(){
                return this.itemView;
            }
        }
    }

}
