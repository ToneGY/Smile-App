package com.example.smile.adapter;

import static com.example.smile.R.drawable.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smile.R;
import com.example.smile.constants.Constants;
import com.example.smile.entity.TodoEntity;
import com.example.smile.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class TodoContentRecycleAdapter extends RecyclerView.Adapter<TodoContentRecycleAdapter.TodoViewHolder> {

    List<TodoEntity> DataBases;
    Context context;
    private OnItemClickListener onItemClickListener;

    List<Integer> selectId = new ArrayList<>();
    private boolean isDeleteMode = false;



    public List<Integer> getSelectId() {
        return selectId;
    }

    public void setSelectId(List<Integer> selectId) {
        this.selectId = selectId;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDeleteMode(boolean isDeleteMode){
        //每次设置删除模式时，清除保存的id
        selectId.clear();
        this.isDeleteMode = isDeleteMode;
        if(DataBases.get(DataBases.size()-1).getNice() == Constants.NICE.NULLBLOCK){
            if(isDeleteMode){
                DataBases.remove(DataBases.size()-1);
            }
        }
        else{
            if(!isDeleteMode){
                TodoEntity nullEntity = new TodoEntity();
                nullEntity.setNice(Constants.NICE.NULLBLOCK);
                nullEntity.setId(-1);
                DataBases.add(nullEntity);
            }
        }
        //通知适配器数据改变，重新渲染
        notifyDataSetChanged();
    }

    public boolean getIsDeleteMode(){
        return isDeleteMode;
    }


    public TodoContentRecycleAdapter(Context context) {
        super();
        this.context = context;
    }

    public List<TodoEntity> getDataBases() {
        return DataBases;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataBases(List<TodoEntity> dataBases) {
        DataBases = dataBases;
        TodoEntity nullEntity = new TodoEntity();
        nullEntity.setId(-1);
        nullEntity.setNice(Constants.NICE.NULLBLOCK);
        dataBases.add(nullEntity);
        notifyDataSetChanged();
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(parent.getContext());
        View itemView =inflater.inflate(R.layout.todo_content_item, parent, false);
        return new TodoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoEntity entity = DataBases.get(position);
        setBackgroudByNice(holder.item, entity.getNice());
        if(Constants.NICE.NULLBLOCK != entity.getNice()){
            holder.begin_time.setText(TimeUtil.getDateAndTime(entity.getBegin_time()));
            holder.end_time.setText(TimeUtil.getDateAndTime(entity.getEnd_time()));
            holder.text.setText(entity.getTitle());
            holder.getItemView().setVisibility(View.VISIBLE);
            holder.getItemView().setMinimumHeight(context.getResources().getDimensionPixelOffset(R.dimen.todo_list_item_height));
        }else if(Constants.NICE.NULLBLOCK == entity.getNice()) {
            holder.getItemView().setMinimumHeight(context.getResources().getDimensionPixelOffset(R.dimen.todo_list_item_end_height));
            holder.getItemView().setVisibility(View.INVISIBLE);
        }

        if (!isDeleteMode) {
            holder.delete.setVisibility(View.INVISIBLE);
        } else {
            holder.delete.setVisibility(View.VISIBLE);
            if (selectId.contains(entity.getId())) holder.de_sel.setVisibility(View.VISIBLE);
            else holder.de_sel.setVisibility(View.INVISIBLE);
        }

        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isDeleteMode) onItemClickListener.onItemClick(view, holder.getLayoutPosition());
                else{
                    if(!selectId.contains(entity.getId())){
                        selectId.add(entity.getId());
                        holder.de_sel.setVisibility(View.VISIBLE);
                    } else{
                        selectId.remove(entity.getId());
                        holder.de_sel.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        holder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isDeleteMode) {
                    onItemClickListener.onItemLongClick(view, holder.getLayoutPosition());
                    selectId.add(entity.getId());
                    holder.de_sel.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });


    }

    public void setBackgroudByNice(RelativeLayout item, int nice){
        if(Constants.NICE.VERY_IMPORTANT == nice) item.setBackgroundResource(content_border_veryimportant);
        else if(Constants.NICE.IMPORTANT == nice) item.setBackgroundResource(content_border_important);
        else if(Constants.NICE.EASY == nice) item.setBackgroundResource(content_border_easy);
        else if(Constants.NICE.VERY_EASY == nice) item.setBackgroundResource(content_border_veryeasy);
        else item.setBackgroundResource(content_border_done);
    }

    @Override
    public int getItemCount() {
        return DataBases == null ? 0 : DataBases.size();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }


    static class TodoViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout item;
        TextView begin_time;
        TextView end_time;
        TextView text;

        RelativeLayout delete;
        ImageView de_sel;
        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.todo_content_item_init);
            begin_time = itemView.findViewById(R.id.content_item_todo_begintime);
            end_time = itemView.findViewById(R.id.content_item_todo_endtime);
            text = itemView.findViewById(R.id.content_item_text);
            delete = itemView.findViewById(R.id.todo_item_delete_button);
            de_sel = itemView.findViewById(R.id.todo_content_item_selected);
        }

        public View getItemView(){
            return this.itemView;
        }
    }
}
