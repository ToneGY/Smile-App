package com.example.smile.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.example.smile.R;
import com.example.smile.util.DensityUtil;
import com.example.smile.util.FileIconUtil;
import com.example.smile.util.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * 作者：Leon
 * 时间：2017/3/15 15:47
 */
public class PathAdapter extends RecyclerView.Adapter<PathAdapter.PathViewHolder> {
    public interface OnItemClickListener {
        void click(int position);
    }

    public interface OnItemLongClickListener {
        void long_click(int position, View view);
    }

    public interface OnCancelChoosedListener {
        void cancelChoosed(CheckBox checkBox);
    }

    private final String TAG = "FilePickerLeon";
    private List<File> mListData;
    private Context mContext;
    public OnItemClickListener onItemClickListener;
    public OnItemLongClickListener onItemLongClickListener;
    private FileFilter mFileFilter;
    private boolean[] mCheckedFlags;
    private boolean onSelecting;
    private boolean mIsGreater;
    private long mFileSize;

    public PathAdapter(List<File> mListData, Context mContext, FileFilter mFileFilter, boolean mIsGreater, long mFileSize) {
        this.mListData = mListData;
        this.mContext = mContext;
        this.mFileFilter = mFileFilter;
        this.onSelecting = false;
        this.mIsGreater = mIsGreater;
        this.mFileSize = mFileSize;
        mCheckedFlags = new boolean[mListData.size()];
    }

    @Override
    public PathViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.note_file_item, null);
        PathViewHolder pathViewHolder = new PathViewHolder(view);
        return pathViewHolder;
    }

    public void setOnSelecting(boolean onSelecting){
        this.onSelecting = onSelecting;
    }

    @Override
    public int getItemCount() {
        return mListData.size() + 1;
    }

    @Override
    public void onBindViewHolder(final PathViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if(holder == null) return;
        if(position == getItemCount() - 1){
            RecyclerView.LayoutParams params =(RecyclerView.LayoutParams) holder.layoutRoot.getLayoutParams();
            if(params == null) {
                params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,DensityUtil.dp2px(mContext, 100));
                holder.layoutRoot.setLayoutParams(params);
            }else {

                params.height = DensityUtil.dp2px(mContext, 100);
                holder.layoutRoot.setLayoutParams(params);
            }
            holder.layoutRoot.setVisibility(View.INVISIBLE);
            return;
        }else{
            RecyclerView.LayoutParams params =(RecyclerView.LayoutParams) holder.layoutRoot.getLayoutParams();
            if(params!=null) {
                params.height = DensityUtil.dp2px(mContext, 60);
                holder.layoutRoot.setLayoutParams(params);
                holder.layoutRoot.setVisibility(View.VISIBLE);
            }
        }
        final File file = mListData.get(position);
        if (file.isFile()) {
            updateFileIconStyle(holder.ivType, file.getName());
            holder.tvName.setText(file.getName());
            holder.tvDetail.setText(mContext.getString(R.string.lfile_FileSize) + " " + FileUtil.getReadableFileSize(file.length()));
            holder.cbChoose.setVisibility(View.VISIBLE);
        } else {
            updateFloaderIconStyle(holder.ivType);
            holder.tvName.setText(file.getName());
            //文件大小过滤
            List files = FileUtil.getFileList(file.getAbsolutePath(), mFileFilter, mIsGreater, mFileSize);
            if (files == null) {
                holder.tvDetail.setText("0 " + mContext.getString(R.string.lfile_LItem));
            } else {
                holder.tvDetail.setText(files.size() + " " + mContext.getString(R.string.lfile_LItem));
            }
            holder.cbChoose.setVisibility(View.GONE);
        }
        if (!onSelecting) {
            holder.cbChoose.setVisibility(View.GONE);
        }
        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSelecting && file.isFile()) {
                    holder.cbChoose.setChecked(!holder.cbChoose.isChecked());
                }else{
                    holder.cbChoose.setChecked(false);
                }
                onItemClickListener.click(position);
            }
        });

        holder.layoutRoot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onItemLongClickListener.long_click(position, view);
                return true;
            }
        });
        holder.cbChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //同步复选框和外部布局点击的处理
                onItemClickListener.click(position);
            }
        });
        holder.cbChoose.setOnCheckedChangeListener(null);//先设置一次CheckBox的选中监听器，传入参数null
        holder.cbChoose.setChecked(mCheckedFlags[position]);//用数组中的值设置CheckBox的选中状态
        //再设置一次CheckBox的选中监听器，当CheckBox的选中状态发生改变时，把改变后的状态储存在数组中
        holder.cbChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mCheckedFlags[position] = b;
            }
        });
    }

    private void updateFloaderIconStyle(ImageView imageView) {
        imageView.setBackgroundResource(R.drawable.folder);
    }

    private void updateFileIconStyle(ImageView imageView, String file_name) {
        imageView.setBackgroundResource(FileIconUtil.getIconBySuffix(file_name));
    }

    /**
     * 设置监听器
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
        this.onItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 设置数据源
     *
     * @param mListData
     */
    public void setmListData(List<File> mListData) {
        this.mListData = mListData;
        mCheckedFlags = new boolean[mListData.size()];
    }


    /**
     * 设置是否全选
     *
     * @param isAllSelected
     */
    public void updateAllSelelcted(boolean isAllSelected) {

        for (int i = 0; i < mCheckedFlags.length; i++) {
            mCheckedFlags[i] = isAllSelected;
        }
        notifyDataSetChanged();
    }

    class PathViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout layoutRoot;
        private ImageView ivType;
        private TextView tvName;
        private TextView tvDetail;
        private CheckBox cbChoose;

        public PathViewHolder(View itemView) {
            super(itemView);
            ivType = (ImageView) itemView.findViewById(R.id.iv_type);
            layoutRoot = (RelativeLayout) itemView.findViewById(R.id.layout_item_root);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDetail = (TextView) itemView.findViewById(R.id.tv_detail);
            cbChoose = (CheckBox) itemView.findViewById(R.id.cb_choose);
        }
    }
}


