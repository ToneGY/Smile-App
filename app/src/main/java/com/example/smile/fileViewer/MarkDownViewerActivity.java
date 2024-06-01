package com.example.smile.fileViewer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smile.R;
import com.example.smile.util.FileUtil;


import org.commonmark.node.Node;

import java.io.File;

import io.noties.markwon.Markwon;

public class MarkDownViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mark_down_viewer);
        Intent intent = getIntent();
        String url = intent.getStringExtra("markdown_file");
        TextView textView = findViewById(R.id.markdown_view);
        Markwon markwon = Markwon.create(this);
        textView.post(new Runnable() {
            @Override
            public void run() {
                String str = FileUtil.readFileByUrl(url);
                Log.e("markdown", str);
                Node node = markwon.parse(str);
                Spanned spanned = markwon.render(node);
                markwon.setParsedMarkdown(textView, spanned);
            }
        });
    }
}
