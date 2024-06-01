package com.example.smile.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.TaskStackBuilder;

import com.example.smile.R;
import com.example.smile.entity.Elearning.HWDetailEntity;
import com.example.smile.interfaceclass.ProgressListener;
import com.example.smile.util.DensityUtil;
import com.example.smile.util.DownLoadUtil;
import com.example.smile.util.ElearningUtil;
import com.example.smile.util.FileIconUtil;
import com.example.smile.util.FileUtil;
import com.zzhoujay.markdown.MarkDown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NoteHWDetailActivity extends AppCompatActivity {
    LinearLayout linearLayout;
    TextView file;
    TextView progress;
    ProgressBar progressBar;
    Button button;
    WindowManager windowManager;
    WindowManager.LayoutParams params;
    boolean flag = false;
    Thread thread;
    String curFileName;

    static final Integer HANDLER_CREATE = 1;
    static final Integer HANDLER_MODIFY = 2;
    static final Integer HANDLER_REMOVE = 3;
    public static String DOWNLOAD_DONE="note_hw_detail_download_done";
    private class MyHandler extends Handler{
        private WeakReference<NoteHWDetailActivity> weakReference;

        public MyHandler(NoteHWDetailActivity weakReference) {
            this.weakReference = new WeakReference(weakReference);
        }


        @Override
        public void handleMessage(@NonNull Message msg) {
            NoteHWDetailActivity activity = weakReference.get();
            super.handleMessage(msg);
            if(activity == null) return;
            if(msg.what == HANDLER_MODIFY) {
                int p = (Integer) msg.obj;
                progress.setText("当前进度：" + p + "%");
                progressBar.setProgress(p);
            } else if(msg.what == HANDLER_REMOVE) {
                Toast.makeText(NoteHWDetailActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                if (flag) {
                    try {
                        windowManager.removeView(linearLayout);
                        progress.setText("当前进度：0%");
                    } catch (Exception e) {
                    }
                }
                Intent intent = new Intent(DOWNLOAD_DONE);
                sendBroadcast(intent);
                flag = false;
                thread = null;
            }else if(msg.what == HANDLER_CREATE){
                HWDetailEntity entity = (HWDetailEntity)msg.obj;
                loading.dismiss();
                start(entity);

            }
        }
    }
    private MyHandler handler = new MyHandler(NoteHWDetailActivity.this);
    AlertDialog loading;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_hw_detail);
        Intent intent = getIntent();
        String main_url = intent.getStringExtra("url");

        View progressBar = LayoutInflater.from(this).inflate(R.layout.loading, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this,R.style.TransparentDialog);
        alert.setView(progressBar);
        loading = alert.create();
        loading.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
               HWDetailEntity entity =  ElearningUtil.getHomeWorkDetailInMarkdwonType(main_url);
               Message msg = Message.obtain();
               msg.what = HANDLER_CREATE;
               msg.obj = entity;
               handler.sendMessage(msg);
            }
        }).start();
    }


    void start(HWDetailEntity entity){
        String text = entity.getMarkdown();
        TextView textView = findViewById(R.id.note_detail);
        ViewGroup cardView1 = findViewById(R.id.note_detail_card_1);
        ViewGroup cardView2 = findViewById(R.id.note_detail_card_2);

        linearLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.note_detail_down_load,null);
        file = linearLayout.findViewById(R.id.progress_filename);
        progress = linearLayout.findViewById(R.id.progress);
        progressBar = linearLayout.findViewById(R.id.progress_bar);
        button = linearLayout.findViewById(R.id.download_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flag){
                    windowManager.removeView(linearLayout);
                    progress.setText("当前进度：0%");
                    flag = false;
                }
            }});
        windowManager = getWindow().getWindowManager();
        params = new WindowManager.LayoutParams();
        params.x = 0;
        params.y = 20;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = DensityUtil.dp2px(NoteHWDetailActivity.this, 80);
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSPARENT;


        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        List<String> name = entity.getName();
        List<String> url = entity.getUrl();
        for(int i = 0; i < name.size(); i++) {
            if (Objects.equals(name.get(i).trim(), "")) continue;
            View v = vi.inflate(R.layout.note_detail_file_item, null);
            TextView t = v.findViewById(R.id.note_detail_file_name);
            ImageView icon = v.findViewById(R.id.note_detail_file_type);
            icon.setImageResource(FileIconUtil.getIconBySuffix(name.get(i)));
            t.setText(name.get(i));
            int finalI = i;
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!flag && thread == null) {
                        file.setText(t.getText());
                        flag = true;
                        windowManager.addView(linearLayout, params);
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                curFileName = t.getText().toString();
                                ElearningUtil.download(NoteHWDetailActivity.this, url.get(finalI), callback);
                            }
                        });
                        thread.start();
                    }
                }
            });
            cardView1.addView(v, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(NoteHWDetailActivity.this, 35)));
        }

        List<String> done_name = entity.getDone_name();
        List<String> done_url = entity.getDone_url();
        for(int i = 0; i < done_name.size(); i++){
            if(Objects.equals(done_name.get(i).trim(), "")) continue;
            View v = vi.inflate(R.layout.note_detail_file_item, null);
            ImageView icon = v.findViewById(R.id.note_detail_file_type);
            TextView t = v.findViewById(R.id.note_detail_file_name);
            icon.setImageResource(FileIconUtil.getIconBySuffix(done_name.get(i)));

            t.setText(done_name.get(i));
            int finalI = i;
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!flag && thread == null){
                        file.setText(t.getText());
                        flag = true;
                        windowManager.addView(linearLayout, params);
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                curFileName = t.getText().toString();
                                ElearningUtil.download(NoteHWDetailActivity.this, done_url.get(finalI), callback);
                            }
                        });
                        thread.start();
                    }
                }
            });
            cardView2.addView(v, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(this, 35)));
        }

        if(entity.getScore() != ""){
            TextView tv = new TextView(this);
            tv.setText(entity.getScore());
            tv.setBackgroundColor(Color.rgb(255,255,255));
            cardView2.addView(tv, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        if(entity.getComments().size()!=0){
            for(int i = 0; i < entity.getComments().size(); i++){
                TextView tv = new TextView(this);
                tv.setText(entity.getComments().get(i));
                tv.setBackgroundColor(Color.rgb(255,255,255));
                cardView2.addView(tv, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }


        textView.post(new Runnable() {
            @Override
            public void run() {
                Spanned spanned = MarkDown.fromMarkdown(text, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return null;
                    }
                }, textView);
                textView.setText(spanned);
            }
        });
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
                String suffix;

                String trueName = response.headers().get("Content-Disposition");
                trueName = trueName.substring(trueName.indexOf("filename=\"") + 10, trueName.indexOf("\";"));
                suffix = trueName.substring(trueName.lastIndexOf("."));
                if(curFileName == null || curFileName.length()==0) {
                    fileName = trueName;
                }else{
                    fileName = curFileName;
                }
                int i = 0;
                if (!FileUtil.isSDFileExist(NoteHWDetailActivity.this, saveDir))
                    FileUtil.creatSDDir(NoteHWDetailActivity.this, saveDir);
                String prefix;
                int index = fileName.lastIndexOf(".");
                if(index >=0 && fileName.substring(fileName.lastIndexOf(".")) == suffix){
                    prefix= fileName.substring(0, fileName.lastIndexOf("."));
                }else{
                    prefix = fileName;
                }
                fileName = prefix+suffix;
                while (FileUtil.isSDFileExist(NoteHWDetailActivity.this, saveDir + fileName)) {
                    fileName = prefix + "(" + ++i + ")" + suffix;
                }
                File file = FileUtil.creatSDFile(NoteHWDetailActivity.this, saveDir + fileName);

                fos = new FileOutputStream(file.getPath());
                long sum = 0;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                    sum += len;
                    int progress_ = (int) (sum * 1.0f / total * 100);
                    Message message = Message.obtain();
                    message.what = HANDLER_MODIFY;
                    message.obj = progress_;
                    handler.sendMessage(message);
                }
                fos.flush();
                Message message = Message.obtain();
                message.what = HANDLER_REMOVE;
                handler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("fail", "失败");
            }

            if (is != null) is.close();
            if (fos != null) fos.close();
        }
    };
}
