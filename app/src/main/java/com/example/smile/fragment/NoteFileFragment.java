package com.example.smile.fragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smile.R;
import com.example.smile.activity.NoteHWDetailActivity;
import com.example.smile.fileViewer.MarkDownViewerActivity;
import com.example.smile.fileViewer.PDFViewerActivity;
import com.example.smile.adapter.PathAdapter;
import com.example.smile.constants.Constants;
import com.example.smile.entity.ParamEntity;
import com.example.smile.util.DensityUtil;
import com.example.smile.util.FileUtil;
import com.example.smile.util.LFileFilter;
import com.example.smile.util.TypeUtil;
import com.example.smile.view.CustomPopWindow;
import com.example.smile.view.EmptyRecyclerView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NoteFileFragment extends Fragment {
    private final String TAG = "FilePickerLeon";

    class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                    Message msg = Message.obtain();
                    msg.what = HANDLE_MENU;
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    View view;

    private EmptyRecyclerView mRecylerView;
    private View mEmptyView;
    private TextView mTvPath;
    private ImageView mTvBack;
    private String mPath;
    private List<File> mListFiles;
    private PathAdapter mPathAdapter;
    private ParamEntity mParamEntity;
    private LFileFilter mFilter;
    private boolean mIsAllSelected = false;
    private boolean mOnSelecting = false;
    private ArrayList<String> mListNumbers = new ArrayList<String>();//存放选中条目的数据地址
    static Integer HANDLE_MENU = 0;
    static Integer HANDLE_INIT = 1;
    static Integer HANDLE_MENU_DELETE = 2;
    static Integer HANDLE_MOVE = 3;
    static Integer HANDLE_MOVE_INIT = 4;
    private class MyHandler extends Handler {
        private WeakReference<NoteFileFragment> weakReference;

        public MyHandler(NoteFileFragment weakReference) {
            this.weakReference = new WeakReference(weakReference);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            NoteFileFragment activity = weakReference.get();
            super.handleMessage(msg);
            if(activity == null) return;
            if(msg.what == HANDLE_MENU) {
                mPathAdapter.setmListData(mListFiles);
                mPathAdapter.notifyDataSetChanged();
                mRecylerView.scrollToPosition(0);

                mPathAdapter.updateAllSelelcted(false);
                mIsAllSelected = false;

                setShowPath(mPath);

                mListNumbers.clear();
                //mBtnAddBook.setText(getString(R.string.lfile_Selected));
                //清除添加集合中数据
                if (mParamEntity.getAddText() != null) {
                    //mBtnAddBook.setText(mParamEntity.getAddText());
                } else {
                    //mBtnAddBook.setText(R.string.lfile_Selected);
                }
            }
            else if(msg.what == HANDLE_INIT){
                initView(view);
                mRecylerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                while(mPathAdapter == null);
                mRecylerView.setAdapter(mPathAdapter);
                mRecylerView.setmEmptyView(mEmptyView);
                if (!checkSDState()) {
                    Toast.makeText(getContext(), R.string.lfile_NotFoundPath, Toast.LENGTH_SHORT).show();
                }
                setShowPath(mPath);
                initListener();
            }else if(msg.what == HANDLE_MENU_DELETE){
                mPathAdapter.setmListData(mListFiles);
                mPathAdapter.notifyDataSetChanged();
                mRecylerView.scrollToPosition((int)msg.obj);
            }else if(msg.what == HANDLE_MOVE){
                pathAdapter.setmListData(folders);
                pathAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(0);
            }else if(msg.what == HANDLE_MOVE_INIT){
                while(pathAdapter == null);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                recyclerView.setAdapter(pathAdapter);
            }
        }
    }
    private MyHandler handler = new MyHandler(NoteFileFragment.this);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("debug","NoteFileFragment_onCreate");

        MyBroadcastReceiver mbcr = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(NoteHWDetailActivity.DOWNLOAD_DONE);
        requireContext().registerReceiver(mbcr,intentFilter);

        mParamEntity = new ParamEntity();
        mParamEntity.setTitle("mTitle");
        //mParamEntity.setTheme(0);//白天or黑夜
        //mParamEntity.setTitleColor("blue");
        //mParamEntity.setTitleStyle(mTitleStyle);
        //mParamEntity.setBackgroundColor(mBackgroundColor);
        mParamEntity.setAddText("");
        mParamEntity.setIconStyle(Constants.ICON_STYLE_BLUE);
        //mParamEntity.setFileTypes(mFileTypes);
        mParamEntity.setNotFoundFiles("mNotFoundFiles");
        mParamEntity.setPath(getContext().getExternalFilesDir("") + "/smile/elearning/");
        mParamEntity.setFileSize(1000 * 1024);
        mParamEntity.setGreater(false);
        //getActivity().setTheme(mParamEntity.getTheme());
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPath = mParamEntity.getPath();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("debug","NoteFileFragment_onCreateView");
        view=inflater.inflate(R.layout.note_file_fragment, container,false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mFilter = new LFileFilter(mParamEntity.getFileTypes());
                mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                mPathAdapter = new PathAdapter(mListFiles, getContext(), mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                Message msg = Message.obtain();
                msg.what = HANDLE_INIT;
                handler.sendMessage(msg);
            }
        }).start();
        return view;
    }

    /**
     * 添加点击事件处理
     */
    private void initListener() {
        // 返回目录上一级
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempPath = new File(mPath).getParent();
                if (tempPath == null) {
                    return;
                }
                mPath = tempPath;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                        Message msg = Message.obtain();
                        msg.what = HANDLE_MENU;
                        handler.sendMessage(msg);
                    }
                }).start();

            }
        });
        mPathAdapter.setOnItemClickListener(new PathAdapter.OnItemClickListener() {
            @Override
            public void click(int position) {
                if(mOnSelecting){
                    if(mListFiles.get(position).isDirectory()) return;
                    if (mListNumbers.contains(mListFiles.get(position).getAbsolutePath())) {
                        mListNumbers.remove(mListFiles.get(position).getAbsolutePath());
                    } else {
                        mListNumbers.add(mListFiles.get(position).getAbsolutePath());
                    }
                    return;
                }
                if (mListFiles.get(position).isDirectory()) {
                    //如果当前是目录，则进入继续查看目录
                    chekInDirectory(position);
                }else{
                    String name = mListFiles.get(position).getName();
                    if(name.endsWith(".pdf")){
                        Intent intent = new Intent(getContext(), PDFViewerActivity.class);
                        intent.putExtra("pdf_file", mListFiles.get(position).getAbsolutePath());
                        startActivity(intent);
                    }else if(name.endsWith(".md")){
                        Intent intent = new Intent(getContext(), MarkDownViewerActivity.class);
                        intent.putExtra("markdown_file", mListFiles.get(position).getAbsolutePath());
                        startActivity(intent);
                    }else{
//                        Uri uri = Uri.fromFile(mListFiles.get(position));
//                        FileViewer.startMuPDFActivityByUri(getContext(), uri);
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri uri = FileProvider.getUriForFile(getContext(),"fileprovider",new File(mListFiles.get(position).getAbsolutePath()));
                        intent.setDataAndType(uri, TypeUtil.getMIMEType(mListFiles.get(position).getAbsolutePath()));
                        Log.e("filetype",TypeUtil.getMIMEType(mListFiles.get(position).getAbsolutePath()));
                        try{startActivity(intent);}catch (ActivityNotFoundException e){
                            Toast.makeText(getContext(), "没有可以用来打开的应用", Toast.LENGTH_LONG);
                        }
                    }
                }


            }
        });

        mPathAdapter.setOnItemLongClickListener(new PathAdapter.OnItemLongClickListener() {
            @Override
            public void long_click(int position, View view) {
                if (mListFiles.get(position).isFile()) {
                    myPopupMenu(view, position);
                }else{
                    folderPopupMenu(view,position);
                }
            }
        });
    }


    /**
     * 点击进入目录
     *
     * @param position
     */
    private void chekInDirectory(int position) {
        mPath = mListFiles.get(position).getAbsolutePath();
        setShowPath(mPath);
        //更新数据源
        new Thread(new Runnable() {
            @Override
            public void run() {
                mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                Message msg = Message.obtain();
                msg.what = HANDLE_MENU;
                handler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 完成提交
     */
    private void chooseDone() {
        //判断是否数量符合要求
        if (mParamEntity.isChooseMode()) {
            if (mParamEntity.getMaxNum() > 0 && mListNumbers.size() > mParamEntity.getMaxNum()) {
                Toast.makeText(getContext(), R.string.lfile_OutSize, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra("paths", mListNumbers);
        intent.putExtra("path", mTvPath.getText().toString().trim());
        //setResult(RESULT_OK, intent);
    }

    /**
     * 初始化控件
     */
    private void initView(View view) {
        mRecylerView = (EmptyRecyclerView) view.findViewById(R.id.recylerview);
        mTvPath = (TextView) view.findViewById(R.id.tv_path);
        mTvBack = (ImageView) view.findViewById(R.id.tv_back);
        mEmptyView = view.findViewById(R.id.empty_view);
    }

    /**
     * 检测SD卡是否可用
     */
    private boolean checkSDState() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 显示顶部地址
     *
     * @param path
     */
    private void setShowPath(String path) {
        String show_path = null;
        if(Objects.equals(path, getContext().getExternalFilesDir("") + "/smile/elearning/") ||
            Objects.equals(path, getContext().getExternalFilesDir("") + "/smile/elearning")) {
            mTvBack.setVisibility(View.INVISIBLE);
        }
        else {
            mTvBack.setVisibility(View.VISIBLE);
        }

        show_path = getWantedPath(path);
        mTvPath.setText(show_path);

    }

    public String getWantedPath(String path){
        String show_path;
        show_path = path.substring(path.indexOf("smile/elearning"));
        show_path = show_path.replaceAll("/"," > ");
        if(show_path.endsWith(" > ")){
            show_path = show_path.substring(0, show_path.length() - 3);
        }
        return show_path;
    }


    private void folderPopupMenu(View v, int position){
        View  contentView = LayoutInflater.from(getContext()).inflate(R.layout.note_folder_menu, null);
        while (contentView==null);

        CustomPopWindow mCustomPopWindow = new CustomPopWindow.PopupWindowBuilder(getContext())
                .setView(contentView)
                .setFocusable(true)
                .enableBackgroundDark(true).setBgDarkAlpha(0.8f)
                .setAnimationStyle(R.style.CustomPopWindowStyle)
                .create();
        handleFolderMenuLogic(mCustomPopWindow, contentView, position);
        if(v.getY() < mRecylerView.getHeight()/4){
            mCustomPopWindow.showAsDropDown(v,500,-50);
        }else if(v.getY() < mRecylerView.getHeight()/2){
            mCustomPopWindow.showAsDropDown(v,500,-mCustomPopWindow.getHeight()+50);
        }else{
            mCustomPopWindow.showAsDropDown(v,500,-v.getHeight()-mCustomPopWindow.getHeight()+20);
        }
    }

    private void handleFolderMenuLogic(CustomPopWindow mCustomPopWindow, View contentView, int position) {
        File file = mListFiles.get(position);
        contentView.findViewById(R.id.note_folder_menu_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(file.listFiles().length!=0) {
                    Toast.makeText(getContext(),"当前目录非空，无法删除", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        file.delete();
                        mListFiles.remove(position);
                        Message msg = Message.obtain();
                        msg.what = HANDLE_MENU_DELETE;
                        msg.obj = position -1;
                        handler.sendMessage(msg);
                    }
                }).start();
                mCustomPopWindow.dissmiss();
            }
        });
        contentView.findViewById(R.id.note_folder_menu_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCustomPopWindow.dissmiss();
                View renameView = LayoutInflater.from(getContext()).inflate(R.layout.note_file_rename_alert, null);
                EditText editText = renameView.findViewById(R.id.note_rename_name);
                Button confirm = renameView.findViewById(R.id.note_rename_confirm);
                Button cancel = renameView.findViewById(R.id.note_rename_cancel);
                TextView cur_name = renameView.findViewById(R.id.note_rename_cur_name);
                cur_name.setText(file.getName());
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                AlertDialog alertDialog = alertBuilder.setView(renameView).create();
                alertDialog.show();

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = editText.getText().toString();
                        if(name == null || name.trim()=="") return;
                        final String new_name = name.trim();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                file.renameTo(new File((file.getParentFile().getAbsolutePath().endsWith("/") ? file.getParentFile().getAbsolutePath() : file.getParentFile().getAbsolutePath()+"/")+ new_name));
                                mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                                Message msg = Message.obtain();
                                msg.what = HANDLE_MENU;
                                handler.sendMessage(msg);
                            }
                        }).start();
                        alertDialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

            }
        });
    }

    private void myPopupMenu(View v, int position) {
        View  contentView = LayoutInflater.from(getContext()).inflate(R.layout.note_file_menu, null);


        CustomPopWindow mCustomPopWindow = new CustomPopWindow.PopupWindowBuilder(getContext())
                .setView(contentView)
                .setFocusable(true)
                .enableBackgroundDark(true).setBgDarkAlpha(0.8f)
                .setAnimationStyle(R.style.CustomPopWindowStyle)
                .create();
        handleMenuLogic(mCustomPopWindow, contentView, position);
        if(v.getY() < mRecylerView.getHeight()/4){
            mCustomPopWindow.showAsDropDown(v,500,-50);
        }else if(v.getY() < mRecylerView.getHeight()/2){
            mCustomPopWindow.showAsDropDown(v,500,-mCustomPopWindow.getHeight()+50);
        }else{
            mCustomPopWindow.showAsDropDown(v,500,-v.getHeight()-mCustomPopWindow.getHeight()+20);
        }
    }
    View moveView;
    RecyclerView recyclerView;
    Button newFolder;
    EditText folderName;
    Button confirm;
    Button cancel;
    TextView curText;
    TextView nextText;
    boolean oncreating = false;
    String nextPath = null;
    PathAdapter pathAdapter;
    List<File> folders = new ArrayList<>();
    public void handleMenuLogic(CustomPopWindow customPopWindow, View contentView, int position){
        File file = mListFiles.get(position);
        contentView.findViewById(R.id.note_file_menu_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        file.delete();
                        mListFiles.remove(position);
                        Message msg = Message.obtain();
                        msg.what = HANDLE_MENU_DELETE;
                        msg.obj = position -1;
                        handler.sendMessage(msg);
                    }
                }).start();
                customPopWindow.dissmiss();
            }
        });
        contentView.findViewById(R.id.note_file_menu_multi_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customPopWindow.dissmiss();
                mOnSelecting = true;
                mPathAdapter.setOnSelecting(true);
                mPathAdapter.notifyDataSetChanged();
                WindowManager windowManager = getActivity().getWindowManager();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params = new WindowManager.LayoutParams();
                params.x = 0;
                params.y = 20;
                params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = DensityUtil.dp2px(getContext(), 100);
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                params.format = PixelFormat.TRANSPARENT;
                View myView = LayoutInflater.from(getContext()).inflate(R.layout.note_file_onselect_menu,null);
                windowManager.addView(myView, params);

                myView.findViewById(R.id.note_selected_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        windowManager.removeView(myView);
                        mListNumbers.clear();
                        mOnSelecting = false;
                        mPathAdapter.setOnSelecting(false);
                        mPathAdapter.notifyDataSetChanged();
                    }
                });

                myView.findViewById(R.id.note_selected_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mListNumbers.size()==0) return;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (String s: mListNumbers){
                                    File f = new File(s);
                                    f.delete();
                                }
                                mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                                mOnSelecting = false;
                                mPathAdapter.setOnSelecting(false);
                                Message msg = Message.obtain();
                                msg.what = HANDLE_MENU_DELETE;
                                msg.obj = 0;
                                handler.sendMessage(msg);
                            }
                        }).start();
                        windowManager.removeView(myView);
                    }
                });


                myView.findViewById(R.id.note_selected_all).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPathAdapter.updateAllSelelcted(!mIsAllSelected);
                        mIsAllSelected = !mIsAllSelected;
                        if (mIsAllSelected) {
                            for (File mListFile : mListFiles) {
                                //不包含再添加，避免重复添加
                                if (!mListFile.isDirectory() && !mListNumbers.contains(mListFile.getAbsolutePath())) {
                                    mListNumbers.add(mListFile.getAbsolutePath());
                                }
                            }
                        } else {
                            mListNumbers.clear();
                        }
                    }
                });

                myView.findViewById(R.id.note_selected_move).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mListNumbers.size()==0) return;
                        windowManager.removeView(myView);
                        moveView = LayoutInflater.from(getContext()).inflate(R.layout.note_file_moveto_alert, null);
                        recyclerView = moveView.findViewById(R.id.note_move_recycle);
                        newFolder  = moveView.findViewById(R.id.note_move_new_folder);
                        folderName = moveView.findViewById(R.id.note_new_folder_create);
                        confirm  = moveView.findViewById(R.id.note_move_confirm);
                        cancel   = moveView.findViewById(R.id.note_move_cancel);
                        curText  = moveView.findViewById(R.id.note_curPWD);
                        nextText = moveView.findViewById(R.id.note_nextPWD);
                        curText.setText(getWantedPath(mPath));
                        nextPath = getContext().getExternalFilesDir("").toString() +"/smile/elearning";
                        nextText.setText(getWantedPath(nextPath));
                        oncreating = false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                folders = FileUtil.getFolderListByDirPath(nextPath, mFilter);
                                pathAdapter = new PathAdapter(folders, getContext(), mFilter, false, 0);
                                Message msg = Message.obtain();
                                msg.what = HANDLE_MOVE_INIT;
                                handler.sendMessage(msg);
                            }
                        }).start();

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setView(moveView);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        while(pathAdapter == null);
                        pathAdapter.setOnItemClickListener(new PathAdapter.OnItemClickListener() {
                            @Override
                            public void click(int position) {
                                if(!oncreating) {
                                    if (folders.get(position).isDirectory()) {
                                        nextPath = folders.get(position).getAbsolutePath();
                                        nextText.setText(getWantedPath(nextPath));
                                        //更新数据源
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                folders = FileUtil.getFileList(nextPath, mFilter,false, 0);
                                                Message msg = Message.obtain();
                                                msg.what = HANDLE_MOVE;
                                                handler.sendMessage(msg);
                                            }
                                        }).start();
                                    }
                                }
                            }
                        });

                        newFolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                oncreating = true;
                                folderName.setVisibility(View.VISIBLE);
                                newFolder.setVisibility(View.GONE);
                            }
                        });

                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(oncreating){
                                    String name = folderName.getText().toString();
                                    if(name == null || name.trim() == "") return;
                                    if(name.contains("/")) return;
                                    oncreating = false;
                                    newFolder.setVisibility(View.VISIBLE);
                                    folderName.setVisibility(View.GONE);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            File newFolder = FileUtil.creatSDDir(getContext(),(nextPath.endsWith("/")?nextPath:nextPath+"/") + name);
                                            folders = FileUtil.getFolderListByDirPath(nextPath, mFilter);
                                            mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                                            Message msg = Message.obtain();
                                            msg.what = HANDLE_MOVE;
                                            handler.sendMessage(msg);
                                            Message msg_ = Message.obtain();
                                            msg_.what = HANDLE_MENU;
                                            handler.sendMessage(msg_);
                                        }
                                    }).start();
                                    return;
                                }
                                for(String s : mListNumbers){
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            File new_file;
                                            File f;
                                            if(!nextPath.endsWith("/")) nextPath+="/";
                                            f = new File(s);
                                            new_file = new File(nextPath + f.getName());
                                            try {
                                                Files.move(f.toPath(), new_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                                            Message msg = Message.obtain();
                                            msg.what = HANDLE_MENU;
                                            handler.sendMessage(msg);
                                        }
                                    }).start();
                                }
                                alertDialog.dismiss();
                                mOnSelecting = false;
                                mListNumbers.clear();
                                mPathAdapter.setOnSelecting(false);
                                mPathAdapter.notifyDataSetChanged();
                            }
                        });

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(oncreating){
                                    oncreating = false;
                                    newFolder.setVisibility(View.VISIBLE);
                                    folderName.setVisibility(View.GONE);
                                }
                                alertDialog.dismiss();
                                mOnSelecting = false;
                                mListNumbers.clear();
                                mPathAdapter.setOnSelecting(false);
                                mPathAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
        contentView.findViewById(R.id.note_file_menu_transmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customPopWindow.dissmiss();
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);//, Uri.parse("mailto:"));
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Uri uri = FileProvider.getUriForFile(getContext(),"fileprovider",new File(mListFiles.get(position).getAbsolutePath()));
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setType(TypeUtil.getMIMEType(mListFiles.get(position).getAbsolutePath()));//此处可发送多种文件
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.putExtra(Intent.EXTRA_STREAM, uri);
                    //intent.setDataAndType(uri, TypeUtil.getMIMEType(mListFiles.get(position).getAbsolutePath()));
//                    intent.setType("image/*");
                    intent.setPackage("com.tencent.mm");
                    startActivity(intent);

                }catch (Exception e){
                    Toast.makeText(getContext(),"没有能够打开该类型文件的应用",Toast.LENGTH_SHORT).show();
                }
            }
        });

        contentView.findViewById(R.id.note_file_menu_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customPopWindow.dissmiss();
                View renameView = LayoutInflater.from(getContext()).inflate(R.layout.note_file_rename_alert, null);
                EditText editText = renameView.findViewById(R.id.note_rename_name);
                Button confirm = renameView.findViewById(R.id.note_rename_confirm);
                Button cancel = renameView.findViewById(R.id.note_rename_cancel);
                TextView cur_name = renameView.findViewById(R.id.note_rename_cur_name);
                String prefix = file.getName().substring(0, file.getName().lastIndexOf("."));
                String suffix = file.getName().substring(file.getName().lastIndexOf("."));
                cur_name.setText(prefix);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                AlertDialog alertDialog = alertBuilder.setView(renameView).create();
                alertDialog.show();

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = editText.getText().toString();
                        if(name == null || name.trim()=="" || name.contains(".")) return;
                        final String new_name = name.trim() + suffix;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                file.renameTo(new File((file.getParentFile().getAbsolutePath().endsWith("/") ? file.getParentFile().getAbsolutePath() : file.getParentFile().getAbsolutePath()+"/")+ new_name));
                                mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                                Message msg = Message.obtain();
                                msg.what = HANDLE_MENU;
                                handler.sendMessage(msg);
                            }
                        }).start();
                        alertDialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        contentView.findViewById(R.id.note_file_menu_move_to).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customPopWindow.dissmiss();
                moveView = LayoutInflater.from(getContext()).inflate(R.layout.note_file_moveto_alert, null);
                recyclerView = moveView.findViewById(R.id.note_move_recycle);
                newFolder  = moveView.findViewById(R.id.note_move_new_folder);
                folderName = moveView.findViewById(R.id.note_new_folder_create);
                confirm  = moveView.findViewById(R.id.note_move_confirm);
                cancel   = moveView.findViewById(R.id.note_move_cancel);
                curText  = moveView.findViewById(R.id.note_curPWD);
                nextText = moveView.findViewById(R.id.note_nextPWD);
                curText.setText(getWantedPath(mPath));
                nextPath = getContext().getExternalFilesDir("").toString() +"/smile/elearning";
                nextText.setText(getWantedPath(nextPath));
                oncreating = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        folders = FileUtil.getFolderListByDirPath(nextPath, mFilter);
                        pathAdapter = new PathAdapter(folders, getContext(), mFilter, false, 0);
                        Message msg = Message.obtain();
                        msg.what = HANDLE_MOVE_INIT;
                        handler.sendMessage(msg);
                    }
                }).start();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setView(moveView);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                while(pathAdapter == null);
                pathAdapter.setOnItemClickListener(new PathAdapter.OnItemClickListener() {
                    @Override
                    public void click(int position) {
                        if(!oncreating) {
                            if (folders.get(position).isDirectory()) {
                                nextPath = folders.get(position).getAbsolutePath();
                                nextText.setText(getWantedPath(nextPath));
                                //更新数据源
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        folders = FileUtil.getFileList(nextPath, mFilter,false, 0);
                                        Message msg = Message.obtain();
                                        msg.what = HANDLE_MOVE;
                                        handler.sendMessage(msg);
                                    }
                                }).start();
                            }
                        }
                    }
                });

                newFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        oncreating = true;
                        folderName.setVisibility(View.VISIBLE);
                        newFolder.setVisibility(View.GONE);
                    }
                });

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(oncreating){
                            String name = folderName.getText().toString();
                            if(name == null || name.trim() == "") return;
                            if(name.contains("/")) return;
                            oncreating = false;
                            newFolder.setVisibility(View.VISIBLE);
                            folderName.setVisibility(View.GONE);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    File newFolder = FileUtil.creatSDDir(getContext(),(nextPath.endsWith("/")?nextPath:nextPath+"/") + name);
                                    folders = FileUtil.getFolderListByDirPath(nextPath, mFilter);
                                    mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());
                                    Message msg = Message.obtain();
                                    msg.what = HANDLE_MOVE;
                                    handler.sendMessage(msg);
                                    Message msg_ = Message.obtain();
                                    msg_.what = HANDLE_MENU;
                                    handler.sendMessage(msg_);
                                }
                            }).start();
                            return;
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File new_file;
                                if(nextPath.endsWith("/")) new_file = new File(nextPath + file.getName());
                                else new_file = new File(nextPath + "/" + file.getName());
                                try {
                                    Files.move(file.toPath(), new_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    mListFiles = FileUtil.getFileList(mPath, mFilter, mParamEntity.isGreater(), mParamEntity.getFileSize());

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Message msg = Message.obtain();
                                msg.what = HANDLE_MENU;
                                handler.sendMessage(msg);
                            }
                        }).start();
                        alertDialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(oncreating){
                            oncreating = false;
                            newFolder.setVisibility(View.VISIBLE);
                            folderName.setVisibility(View.GONE);
                        }
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }
}
