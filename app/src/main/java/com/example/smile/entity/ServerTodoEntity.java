package com.example.smile.entity;

import java.io.Serializable;

public class ServerTodoEntity implements Serializable {
    private Integer id;
    private String title;
    private String content;
    private String begin_time;
    private String end_time;
    private int nice;
    private Integer state;
    private String label;
    private int group_id;

    public ServerTodoEntity(){}

    public ServerTodoEntity(int group_id, TodoEntity serverTodoEntity){
        this.id = serverTodoEntity.getId();
        this.title = serverTodoEntity.getTitle();
        this.content = serverTodoEntity.getContent();
        this.begin_time = serverTodoEntity.getBegin_time();
        this.end_time = serverTodoEntity.getEnd_time();
        this.nice = serverTodoEntity.getNice();
        this.state = serverTodoEntity.getState();
        this.label = serverTodoEntity.getLabel();
        this.group_id = group_id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public void setBegin_time(String begin_time) {
        this.begin_time = begin_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public int getNice() {
        return nice;
    }

    public void setNice(int nice) {
        this.nice = nice;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    @Override
    public String toString() {
        return "ServerTodoEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", begin_time='" + begin_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", nice=" + nice +
                ", state=" + state +
                ", label='" + label + '\'' +
                ", group_id=" + group_id +
                '}';
    }
}
