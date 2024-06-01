package com.example.smile.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smile.R;
import com.example.smile.entity.Elearning.CourseEntity;
import com.example.smile.entity.Elearning.CourseFile;
import com.example.smile.entity.Elearning.CourseFolder;
import com.example.smile.util.DensityUtil;
import com.example.smile.util.ElearningUtil;
import com.example.smile.util.FileIconUtil;
import com.example.smile.util.FileUtil;
import com.example.smile.util.TimeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class CourseActivity extends AppCompatActivity {
    CourseEntity courseEntity;
    List<List<CourseFolder>> courseFolders = new ArrayList<>();
    List<List<CourseFile>> courseFiles = new ArrayList<>();
    RecyclerView recyclerView;
    CourseRecycleAdapter adapter;
    AdapterClick adapterClick;

    TextView title;
    ImageView back;
    TextView folder_count;
    TextView file_count;
    TextView now_path;
    ImageView path_back;
    int level = 0;

    String curFileName;
    static final int INIT = 0;
    static final int DOWNLOAD = 1;
    static final int DOWNLOAD_DONE = 2;
    static final int CHANGE = 3;
    String path;

    TextView progress;
    ProgressBar progressBar;
    TextView file;
    LinearLayout linearLayout;
    boolean flag = false;
    Thread thread;

    AlertDialog loading;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_activity);

        Intent intent = getIntent();
        courseEntity = (CourseEntity) intent.getSerializableExtra("course");

        initView();
        initListener();

        String temp = courseEntity.getShortName().substring(courseEntity.getShortName().indexOf(" ")+1);
        String courseName = temp.substring(0, temp.indexOf(" "));
        String root_url = courseEntity.getHref();

        title.setText(courseName);
        path = "root > ";
        now_path.setText(path);

        View progressBar = LayoutInflater.from(this).inflate(R.layout.loading, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this,R.style.TransparentDialog);
        alert.setView(progressBar);
        loading = alert.create();
        loading.show();

        adapter = new CourseRecycleAdapter(adapterClick);
        recyclerView = findViewById(R.id.course_recylerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                CourseFolder root = ElearningUtil.getRootFolderInfo(root_url);
                level = 0;
                change(root);
                Message msg = Message.obtain();
                msg.what = INIT;
                myHandler.sendMessage(msg);
            }
        }).start();
    }

    void change(CourseFolder root){
        List<CourseFolder> courseFolder = ElearningUtil.getFolderInfo(root);
        List<CourseFile> courseFile = ElearningUtil.getFilesInfo(root);
        if(courseFolder == null) courseFolder = new ArrayList<>();
        else courseFolder.sort(Comparator.comparing(CourseFolder::getCreate_time));
        if(courseFile == null) courseFile = new ArrayList<>();
        else courseFile.sort(Comparator.comparing(CourseFile::getUpdate_time).reversed());
        courseFolders.add(courseFolder);
        courseFiles.add(courseFile);
    }

    MyHandler myHandler = new MyHandler(this);
    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        private WeakReference<CourseActivity> weakReference;

        public MyHandler(CourseActivity weakReference) {
            this.weakReference = new WeakReference(weakReference);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            CourseActivity activity = weakReference.get();
            super.handleMessage(msg);
            if(activity == null) return;
            if(msg.what == INIT){
                file_count.setText(String.valueOf(courseFiles.get(level).size()));
                folder_count.setText(String.valueOf(courseFolders.get(level).size()));
                adapter.setItems(courseFolders.get(level),courseFiles.get(level));
                loading.dismiss();
            }else if(msg.what == DOWNLOAD) {
                int p = (Integer) msg.obj;
                progress.setText("当前进度：" + p + "%");
                progressBar.setProgress(p);
            }else if(msg.what == DOWNLOAD_DONE){
                Toast.makeText(CourseActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                if (flag) {
                    try {
                        windowManager.removeView(linearLayout);
                        progress.setText("当前进度：0%");
                    } catch (Exception e) {
                    }
                }
                Intent intent = new Intent(NoteHWDetailActivity.DOWNLOAD_DONE);
                sendBroadcast(intent);
                flag = false;
                thread = null;
            }else if(msg.what == CHANGE){
                path += msg.obj + " > ";
                now_path.setText(path);
                level++;
                file_count.setText(String.valueOf(courseFiles.get(level).size()));
                folder_count.setText(String.valueOf(courseFolders.get(level).size()));
                adapter.setItems(courseFolders.get(level),courseFiles.get(level));
            }
        }
    }

    WindowManager windowManager;
    WindowManager.LayoutParams params;

    interface AdapterClick{
        abstract void setOnFileClick(CourseFile courseFile);
        abstract void setOnFolderClock(CourseFolder courseFolder);
    }

    public static class CourseRecycleAdapter extends RecyclerView.Adapter<CourseRecycleAdapter.CourseHolder> {
        List<CourseFolder> curCourseFolder;
        List<CourseFile> curCourseFile;
        int folder_count;
        AdapterClick adapterClick;

        public CourseRecycleAdapter(AdapterClick adapterClick) {
            this.adapterClick = adapterClick;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setItems(List<CourseFolder> curCourseFolder, List<CourseFile> curCourseFile){
            this.curCourseFolder = curCourseFolder;
            this.curCourseFile = curCourseFile;
            folder_count = curCourseFolder.size();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CourseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View itemView = layoutInflater.inflate(R.layout.course_file_item,parent,false);
            return new CourseHolder(itemView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull CourseHolder holder, int position) {
            if(position >= folder_count){
                CourseFile courseFile = curCourseFile.get(position-folder_count);
                holder.imageView.setBackgroundResource(FileIconUtil.getIconBySuffix(courseFile.getName()));
                holder.title.setText(courseFile.getName());
                holder.detail.setText("size: "+ FileUtil.getReadableFileSize(courseFile.getSize()) +"\t\t"+ TimeUtil.normalFormat2MyFormat(courseFile.getUpdate_time()));
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapterClick.setOnFileClick(courseFile);
                    }
                });
            }else{
                CourseFolder courseFolder = curCourseFolder.get(position);
                holder.imageView.setBackgroundResource(R.drawable.folder);
                holder.title.setText(courseFolder.getName());
                holder.detail.setText("count: " + (courseFolder.getFolder_count()+courseFolder.getFiles_count()) + "\t\t"+TimeUtil.normalFormat2MyFormat(courseFolder.getUpdate_time()));
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapterClick.setOnFolderClock(courseFolder);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            int size = 0;
            if(curCourseFile!=null) size+=curCourseFile.size();
            if(curCourseFolder!=null) size+=curCourseFolder.size();
            return size;
        }

        static class CourseHolder extends RecyclerView.ViewHolder{
            ImageView imageView;
            TextView title;
            TextView detail;
            public CourseHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.course_file_type);
                title = itemView.findViewById(R.id.course_item_title);
                detail = itemView.findViewById(R.id.course_item_detail);
            }

            public View getItemView(){
                return this.itemView;
            }
        }
    }

    void initListener(){
        adapterClick = new AdapterClick() {
            @Override
            public void setOnFileClick(CourseFile courseFile) {
                if (!flag && thread == null) {
                    file.setText(courseFile.getName());
                    flag = true;
                    windowManager.addView(linearLayout, params);
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            curFileName = courseFile.getName();
                            ElearningUtil.download(CourseActivity.this, courseFile.getUrl(), callback);
                        }
                    });
                    thread.start();
                }
            }

            @Override
            public void setOnFolderClock(CourseFolder courseFolder) {
                if(flag) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        change(courseFolder);
                        Message msg = Message.obtain();
                        msg.what = CHANGE;
                        msg.obj = courseFolder.getName();
                        myHandler.sendMessage(msg);
                    }
                }).start();
            }
        };

        path_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(level == 0) return;
                courseFiles.remove(level);
                courseFolders.remove(level);
                level--;
                path = path.substring(0, path.substring(0, path.lastIndexOf(" > ")).lastIndexOf(" > ") + 3);
                now_path.setText(path);
                adapter.setItems(courseFolders.get(level),courseFiles.get(level));
                file_count.setText(String.valueOf(courseFiles.get(level).size()));
                folder_count.setText(String.valueOf(courseFolders.get(level).size()));
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    void initView(){
        windowManager = getWindow().getWindowManager();
        params = new WindowManager.LayoutParams();
        params.x = 0;
        params.y = 20;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = DensityUtil.dp2px(this, 80);
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSPARENT;

        title = findViewById(R.id.course_title);
        back = findViewById(R.id.course_return);
        folder_count = findViewById(R.id.folder_count);
        file_count = findViewById(R.id.files_count);
        now_path = findViewById(R.id.course_file_path);
        path_back = findViewById(R.id.course_file_back);

        linearLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.note_detail_down_load,null);
        file = linearLayout.findViewById(R.id.progress_filename);
        progress = linearLayout.findViewById(R.id.progress);
        progressBar = linearLayout.findViewById(R.id.progress_bar);
        Button button = linearLayout.findViewById(R.id.download_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flag){
                    windowManager.removeView(linearLayout);
                    progress.setText("当前进度：0%");
                    flag = false;
                    Message msg = Message.obtain();
                    msg.what = CHANGE;
                    myHandler.sendMessage(msg);
                }
            }});
    }

    Callback callback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            InputStream is = null;
            byte[] buf = new byte[2048];
            int len = 0;
            FileOutputStream fos = null;
            String saveDir = "/smile/elearning/";
            // 储存下载文件的目录
            try {
                is = response.body().byteStream();
                long total = response.body().contentLength();
                String fileName;
                if(curFileName == null || curFileName.length()==0) {
                    fileName =response.headers().get("Content-Disposition");
                    fileName = fileName.substring(fileName.indexOf("filename=\"") + 10, fileName.indexOf("\";"));
                }else{
                    fileName = curFileName;
                }
                int i = 0;
                if (!FileUtil.isSDFileExist(CourseActivity.this, saveDir))
                    FileUtil.creatSDDir(CourseActivity.this, saveDir);

                String suffix = fileName.substring(fileName.lastIndexOf("."));
                String prefix = fileName.substring(0, fileName.lastIndexOf("."));
                while (FileUtil.isSDFileExist(CourseActivity.this, saveDir + fileName)) {
                    fileName = prefix + "(" + ++i + ")" + suffix;
                }
                File file = FileUtil.creatSDFile(CourseActivity.this, saveDir + fileName);

                fos = new FileOutputStream(file.getPath());
                long sum = 0;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                    sum += len;
                    int progress_ = (int) (sum * 1.0f / total * 100);
                    Message message = Message.obtain();
                    message.what = DOWNLOAD;
                    message.obj = progress_;
                    myHandler.sendMessage(message);
                }
                fos.flush();
                Message message = Message.obtain();
                message.what = DOWNLOAD_DONE;
                myHandler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("fail", "失败");
            }

            if (is != null) is.close();
            if (fos != null) fos.close();
        }
    };

}
