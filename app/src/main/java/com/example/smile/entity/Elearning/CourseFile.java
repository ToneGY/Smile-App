package com.example.smile.entity.Elearning;

import com.alibaba.fastjson2.JSONObject;

public class CourseFile {
    String url;
    String name;
    String create_time;
    String update_time;
    String type;
    int size;

    public CourseFile(JSONObject jsonObject){
        url = jsonObject.getString("url");
        name = jsonObject.getString("display_name");
        create_time = jsonObject.getString("created_at");
        update_time = jsonObject.getString("updated_at");
        type = jsonObject.getString("content-type");
        size = jsonObject.getInteger("size");
    }

    CourseFile(){}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
