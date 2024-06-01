package com.example.smile.util;

import com.example.smile.R;

import java.util.Objects;

public class FileIconUtil {
    public static int getIconBySuffix(String file_name){
        String suffix = "";
        int index = file_name.lastIndexOf(".");
        if(index>0) suffix = file_name.substring(index);
        if(Objects.equals(suffix, ".pdf")) return R.drawable.file_pdf;
        else if(Objects.equals(suffix, ".ppt")) return R.drawable.pptx;
        else if(Objects.equals(suffix, ".pptx")) return R.drawable.pptx;
        else if(Objects.equals(suffix, ".zip")) return R.drawable.zip;
        else if(Objects.equals(suffix, ".doc")) return R.drawable.docx;
        else if(Objects.equals(suffix, ".docx")) return R.drawable.docx;
        else if(Objects.equals(suffix, ".avi")) return R.drawable.avi;
        else if (Objects.equals(suffix,".md")) return R.drawable.markdown;
        else if (Objects.equals(suffix,".mp4")) return R.drawable.mp4;
        else if (Objects.equals(suffix,".png")) return R.drawable.pic;
        else if (Objects.equals(suffix,".jpeg")) return R.drawable.pic;
        else if (Objects.equals(suffix,".jpg")) return R.drawable.pic;
        else return R.drawable.file_icon;
    }
}
