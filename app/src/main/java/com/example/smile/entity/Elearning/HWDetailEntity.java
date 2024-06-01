package com.example.smile.entity.Elearning;

import java.io.Serializable;
import java.util.List;

public class HWDetailEntity implements Serializable {
    List<String> name;
    List<String> url;
    List<String> done_name;
    List<String> done_url;
    String markdown;
    String score;
    List<String> comments;

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<String> getUrl() {
        return url;
    }

    public void setUrl(List<String> url) {
        this.url = url;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public List<String> getDone_name() {
        return done_name;
    }

    public void setDone_name(List<String> done_name) {
        this.done_name = done_name;
    }

    public List<String> getDone_url() {
        return done_url;
    }

    public void setDone_url(List<String> done_url) {
        this.done_url = done_url;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "HWDetailEntity{" +
                "name=" + name +
                ", url=" + url +
                ", done_name=" + done_name +
                ", done_url=" + done_url +
                ", markdown='" + markdown + '\'' +
                ", score='" + score + '\'' +
                ", comments=" + comments +
                '}';
    }
}
