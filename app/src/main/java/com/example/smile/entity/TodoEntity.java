package com.example.smile.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

public class TodoEntity implements BaseColumns, Parcelable {
    private Integer id;
    private String title;
    private String content;

    private String begin_time;
    private String end_time;

    private int nice;
    private Integer state;
    private String label;

    public TodoEntity(){}

    public TodoEntity(ServerTodoEntity serverTodoEntity){
        this.id = serverTodoEntity.getId();
        this.title = serverTodoEntity.getTitle();
        this.content = serverTodoEntity.getContent();
        this.begin_time = serverTodoEntity.getBegin_time();
        this.end_time = serverTodoEntity.getEnd_time();
        this.nice = serverTodoEntity.getNice();
        this.state = serverTodoEntity.getState();
        this.label = serverTodoEntity.getLabel();
    }

    public TodoEntity(TodoEntity entity) {
        id = entity.getId();
        title = entity.getTitle();
        content = entity.getContent();
        begin_time = entity.getBegin_time();
        end_time = entity.getEnd_time();
        nice = entity.getNice();
        state = entity.getState();
        label = entity.getLabel();
    }

    public TodoEntity(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        title = in.readString();
        content = in.readString();
        begin_time = in.readString();
        end_time = in.readString();
        nice = in.readInt();
        if (in.readByte() == 0) {
            state = null;
        } else {
            state = in.readInt();
        }

        if (in.readByte() == 0) {
            label = null;
        } else {
            label = in.readString();
        }
    }

    public static final Creator<TodoEntity> CREATOR = new Creator<TodoEntity>() {
        @Override
        public TodoEntity createFromParcel(Parcel parcel) {
            return new TodoEntity(parcel);
        }

        @Override
        public TodoEntity[] newArray(int i) {
            return new TodoEntity[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(begin_time);
        dest.writeString(end_time);
        dest.writeInt(nice);
        if (state == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(state);
        }

        if (label == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(label);
        }

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

    public int getNice() {
        return nice;
    }

    public void setNice(int nice) {
        this.nice = nice;
    }

    @NonNull
    @Override
    public String toString() {
        return "TodoEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", begin_time='" + begin_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", nice=" + nice +
                ", state=" + state +
                ", label='" + label + '\'' +
                '}';
    }
}

