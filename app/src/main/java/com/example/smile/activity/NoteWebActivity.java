package com.example.smile.activity;



import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smile.R;
/*
 * 此activity已废弃
 * 先使用NoteHWDetail
 */
public class NoteWebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_web_view);
        WebView webView = findViewById(R.id.note_web_view);
        WebSettings wSet = webView.getSettings();
        wSet.setJavaScriptEnabled(true);
        String html = getIntent().getStringExtra("html");
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8",null);
    }
}
