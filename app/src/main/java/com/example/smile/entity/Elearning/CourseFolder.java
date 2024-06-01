package com.example.smile.entity.Elearning;

import com.alibaba.fastjson2.JSONObject;

public class CourseFolder {
    String name;
    String folder_url;
    String files_url;
    int folder_count;
    int files_count;
    String create_time;
    String update_time;

    public CourseFolder(JSONObject jsonObject){
        name = jsonObject.getString("name");
        folder_url = jsonObject.getString("folders_url");
        files_url = jsonObject.getString("files_url");
        folder_count = jsonObject.getInteger("folders_count");
        files_count = jsonObject.getInteger("files_count");
        create_time = jsonObject.getString("created_at");
        update_time = jsonObject.getString("updated_at");
    }

    CourseFolder(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolder_url() {
        return folder_url;
    }

    public void setFolder_url(String folder_url) {
        this.folder_url = folder_url;
    }

    public String getFiles_url() {
        return files_url;
    }

    public void setFiles_url(String files_url) {
        this.files_url = files_url;
    }

    public int getFolder_count() {
        return folder_count;
    }

    public void setFolder_count(int folder_count) {
        this.folder_count = folder_count;
    }

    public int getFiles_count() {
        return files_count;
    }

    public void setFiles_count(int files_count) {
        this.files_count = files_count;
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

    @Override
    public String toString() {
        return "CourseFolder{" +
                "name='" + name + '\'' +
                ", folder_url='" + folder_url + '\'' +
                ", files_url='" + files_url + '\'' +
                ", folder_count=" + folder_count +
                ", files_count=" + files_count +
                ", create_time='" + create_time + '\'' +
                ", update_time='" + update_time + '\'' +
                '}';
    }
}
