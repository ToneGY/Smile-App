package com.example.smile.entity.Elearning;

import java.io.Serializable;

public class CourseEntity implements Serializable {
    public String shortName;
    public String href;
    public String term;
    public Integer id;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CourseEntity{" +
                "shortName='" + shortName + '\'' +
                ", href='" + href + '\'' +
                ", term='" + term + '\'' +
                ", id=" + id +
                '}';
    }
}
