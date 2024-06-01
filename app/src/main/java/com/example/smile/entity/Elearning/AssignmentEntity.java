package com.example.smile.entity.Elearning;

import java.io.Serializable;

public class AssignmentEntity implements Serializable {
    Integer id;
    String detail_url;
    String title;
    String date_due;
    String date_cre;
    boolean submitted;
    Integer course_id;
    String submission_types;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDetail_url() {
        return detail_url;
    }

    public void setDetail_url(String detail_url) {
        this.detail_url = detail_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate_due() {
        return date_due;
    }

    public void setDate_due(String date_due) {
        this.date_due = date_due;
    }

    public String getDate_cre() {
        return date_cre;
    }

    public void setDate_cre(String date_cre) {
        this.date_cre = date_cre;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public Integer getCourse_id() {
        return course_id;
    }

    public void setCourse_id(Integer course_id) {
        this.course_id = course_id;
    }

    public String getSubmission_types() {
        return submission_types;
    }

    public void setSubmission_types(String submission_types) {
        this.submission_types = submission_types;
    }

    @Override
    public String toString() {
        return "AssignmentEntity{" +
                "id=" + id +
                ", detail_url='" + detail_url + '\'' +
                ", title='" + title + '\'' +
                ", date_due='" + date_due + '\'' +
                ", date_cre='" + date_cre + '\'' +
                ", submitted=" + submitted +
                ", course_id=" + course_id +
                ", submission_types='" + submission_types + '\'' +
                '}';
    }
}
