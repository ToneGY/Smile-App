package com.example.smile.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.smile.activity.MainActivity;
import com.example.smile.entity.Elearning.AssignmentEntity;
import com.example.smile.entity.Elearning.CourseEntity;
import com.example.smile.entity.Elearning.CourseFile;
import com.example.smile.entity.Elearning.CourseFolder;
import com.example.smile.entity.Elearning.HWDetailEntity;
import com.example.smile.util.Cookie.PersistentCookieStore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class ElearningUtil {


    public static CookieHandler cookieHandler;
    public static HttpLoggingInterceptor logging;
    public static OkHttpClient httpClient;

    public static void all_init(String user_name, String passwd){
        if(httpClient ==null) {
            init(MainActivity.getInstance());
            login(user_name, passwd);
        }
    }

    public static void init(Context ctx) {
        Log.e("elearning", "init");
        cookieHandler = new CookieManager(new PersistentCookieStore(ctx), CookiePolicy.ACCEPT_ALL);

        logging = new HttpLoggingInterceptor();
        httpClient = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieHandler)).addInterceptor(logging).build();

    }

    public static void login(String user_name, String passwd) {
        Log.e("debug","login");
        if(user_name == null || passwd == null) return;
        String url = "https://uis.fudan.edu.cn/authserver/login?service=https%3A%2F%2Felearning.fudan.edu.cn%2Flogin%2Fcas%2F3";
        Request login = new Request.Builder().get().url(url).build();
        try (Response response = httpClient.newCall(login).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String html = response.body().string();
            Document document = (Document) Jsoup.parse(html);
            Elements e1 = document.getElementsByTag("input").select("input[type=hidden]");
            if (!e1.isEmpty()) {
                HashMap<String, String> hash = new HashMap<>();
                RequestBody requestBody = new FormBody.Builder()
                        .add("username", user_name)
                        .add("password", passwd)
                        .add("lt", Objects.requireNonNull(e1.select("input[name=lt]").first()).attr("value"))
                        .add("dllt", Objects.requireNonNull(e1.select("input[name=dllt]").first()).attr("value"))
                        .add("execution", Objects.requireNonNull(e1.select("input[name=execution]").first()).attr("value"))
                        .add("_eventId", Objects.requireNonNull(e1.select("input[name=_eventId]").first()).attr("value"))
                        .add("rmShown", Objects.requireNonNull(e1.select("input[name=rmShown]").first()).attr("value"))
                        .build();
                Request request = new Request.Builder().url(url).post(requestBody).build();
                try (Response response1 = httpClient.newCall(request).execute()) {
                    if (!response1.isSuccessful())
                        throw new IOException("Unexpected code " + response1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<CourseEntity> dash() {
        List<CourseEntity> list = new ArrayList<>();
        Log.e("elearing", "dash");
        String url = "https://elearning.fudan.edu.cn/dash";
        Request dash = new Request.Builder().get().url(url).build();
        try (Response response = httpClient.newCall(dash).execute()) {
            Document document = Jsoup.parse(response.body().string());
            String el = document.getElementsByTag("head").first()
                    .getElementsByTag("script").eq(4).toString();
            String ENV = el.substring(el.indexOf("ENV = ") + 6, el.indexOf("BRANDABLE_CSS_HANDLEBARS_INDEX") - 6);
            JSONObject jsonObject = JSON.parseObject(ENV);
            JSONArray STUDENT_PLANNER_COURSES = jsonObject.getJSONArray("STUDENT_PLANNER_COURSES");
            for (Object js : STUDENT_PLANNER_COURSES) {
                JSONObject course = JSON.parseObject(js.toString());
                CourseEntity ce = new CourseEntity();
                ce.setShortName(course.getString("shortName"));
                ce.setHref(course.getString("href"));
                ce.setTerm(course.getString("term"));
                ce.setId(Integer.valueOf(ce.getHref().substring(ce.getHref().indexOf("courses/") + 8)));
                list.add(ce);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<AssignmentEntity> getHomework(String href) {
        List<AssignmentEntity> assign = new ArrayList<>();
        String url = "https://elearning.fudan.edu.cn/api/v1" + href + "/assignment_groups?exclude_assignment_submission_types%5B%5D=wiki_page&exclude_response_fields%5B%5D=description&exclude_response_fields%5B%5D=rubric&include%5B%5D=assignments&include%5B%5D=discussion_topic&override_assignment_dates=true&per_page=50";
        Request hw = new Request.Builder().get().url(url).build();
        try (Response response = httpClient.newCall(hw).execute()) {
            JSONArray ja = JSONArray.parse(response.body().string()).getJSONObject(0).getJSONArray("assignments");
            for (Object ob : ja) {
                JSONObject jo = JSONObject.parseObject(ob.toString());
                AssignmentEntity ae = new AssignmentEntity();
                ae.setTitle(jo.getString("name"));
                ae.setId(Integer.valueOf(jo.getString("id")));
                ae.setDetail_url(jo.getString("html_url"));
                ae.setDate_cre(jo.getString("created_at"));
                ae.setDate_due(jo.getString("due_at"));
                ae.setCourse_id(jo.getInteger("course_id"));
                ae.setSubmission_types(jo.getJSONArray("submission_types").toString());
                if(ae.getSubmission_types().contains("online_upload")) ae.setSubmitted(jo.getBoolean("has_submitted_submissions"));
                else ae.setSubmitted(true);
                assign.add(ae);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return assign;
    }


    public static String getHomeWorkDetail(String complete_href) {
        String url = complete_href;
        Request request = new Request.Builder().get().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            String html = response.body().string();
            Document document = Jsoup.parse(html);
            return document.getElementById("not_right_side").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static HWDetailEntity getHomeWorkDetailInMarkdwonType(String detail_url) {
        String html = getHomeWorkDetail(detail_url);
        if(html == null || html.trim() == "") return null;
        Document document = Jsoup.parse(html);
        Element t = document.getElementById("content");
        String title = t.getElementsByClass("assignment-title").first() == null ? " " : t.getElementsByClass("assignment-title").first().getElementsByClass("title").text();
        List<String> key = new ArrayList<>();
        List<String> value = new ArrayList<>();
        Elements key_value = t.getElementsByClass("student-assignment-overview").first().getElementsByTag("li");
        for(Element e : key_value){
            String k = e.select("span.title").text();
            String v = "";
            if(k.equals("截止")) v = e.select(".display_date").text() + " " + e.select(".display_time").text();
            else v = e.select(".value").text();
            key.add(k);
            value.add(v);
        }
        String content = t.select(".user_content").text();
        StringBuilder markdown = new StringBuilder("# " + title + "\n");
        for(int i = 0; i < key.size(); i++){
            markdown.append("- **").append(key.get(i)).append("** ").append(value.get(i)).append("\n");
        }
        markdown.append("---\n").append("```html\n").append(content).append("\n```\n");
        HWDetailEntity hwDetailEntity = new HWDetailEntity();
        hwDetailEntity.setMarkdown(markdown.toString());
        Elements a = t.select(".user_content").first().getElementsByTag("a");
        List<String> name = new ArrayList<>();
        List<String> url = new ArrayList<>();
        if(!a.isEmpty()) {
            for (Element ea : a) {
                name.add(ea.text());
                String url_ = ea.attr("href").toString();
                if (!url_.contains("download")) url_ = url_.replace("?wrap=1", "/download");
                url_ += "?download_frd=1";
                url_ = "https://elearning.fudan.edu.cn/" + url_;
                url.add(url_);
            }
        }
        hwDetailEntity.setName(name);
        hwDetailEntity.setUrl(url);
        List<String> done_name = new ArrayList<>();
        List<String> done_url = new ArrayList<>();
        Elements submitted = document.getElementById("right-side-wrapper").getElementsByTag("a");
        if(!submitted.isEmpty()) {
            for (Element s : submitted) {
                if (s.text() != "" && s.text().contains("提交作业详细信息")) continue;
                done_name.add(s.text().replace("下载 ",""));
                done_url.add("https://elearning.fudan.edu.cn/" + s.attr("href"));
            }
        }
        hwDetailEntity.setDone_name(done_name);
        hwDetailEntity.setDone_url(done_url);
        List<String> comments = new ArrayList<>();
        String score = "";
        Element d1 = document.getElementById("right-side-wrapper");
        Element d2 = null;
        Elements divs = null;
        if(d1!=null) d2 = d1.getElementsByClass("content").first();
        if(d2!=null) divs = d2.select("div.module");
        if(divs != null) {
            for (Element div : divs) {
                if (div.text().contains("评分")) {
                    score = "\n" + div.text();
                    score = score.substring(0, score.indexOf("）") + 1).replace("（", " （");
                    score += "\n";
                } else if (div.attr("class").contains("comments")) {
                    Elements com = div.getElementsByClass("comment");
                    for (Element c : com) {
                        comments.add(c.text() + "\n");
                    }
                }
            }
        }
        hwDetailEntity.setScore(score);
        hwDetailEntity.setComments(comments);
        return hwDetailEntity;
    }


    public static void download(Context ctx, String url, Callback callback) {
        Request request = new Request.Builder().url(url).get().build();
        httpClient.newCall(request).enqueue(callback);
    }


    public static void files(String href){
        String url =  "https://elearning.fudan.edu.cn" + href + "/files";
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            String html = response.body().string();
            //Document document = Jsoup.parse(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void announcements(String href){
        String url = "https://elearning.fudan.edu.cn" + href + "/announcements";
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            String html = response.body().string();
            Document document = Jsoup.parse(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static CourseFolder getRootFolderInfo(String courseHerf){
        String url = "https://elearning.fudan.edu.cn/api/v1"+courseHerf+ "/folders/by_path/";
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            JSONObject rootObject = JSON.parseArray(response.body().string()).getJSONObject(0);
            return new CourseFolder(rootObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<CourseFolder> getFolderInfo(CourseFolder courseFolder){
        if(courseFolder == null || courseFolder.getFolder_count() <= 0) return null;
        String folderUrl = courseFolder.getFolder_url() + "?include%5B%5D=user&include%5B%5D=usage_rights&include%5B%5D=enhanced_preview_url&include%5B%5D=context_asset_string&per_page=100&sort=&order=";
        Request request = new Request.Builder().url(folderUrl).get().build();
        List<CourseFolder> courseFolderList = new ArrayList<>();
        try (Response response = httpClient.newCall(request).execute()) {
            JSONArray folderInfo = JSON.parseArray(response.body().string());
            for(int i = 0; i < folderInfo.size(); i++){
                JSONObject jsonObject = folderInfo.getJSONObject(i);
                courseFolderList.add(new CourseFolder(jsonObject));
            }
            return courseFolderList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<CourseFile> getFilesInfo(CourseFolder courseFolder){
        if(courseFolder == null || courseFolder.getFiles_count()<=0) return null;
        String fileUrl = courseFolder.getFiles_url()+ "?include%5B%5D=user&include%5B%5D=usage_rights&include%5B%5D=enhanced_preview_url&include%5B%5D=context_asset_string&per_page=100&sort=&order=";
        Request request = new Request.Builder().url(fileUrl).get().build();
        List<CourseFile> courseFileList = new ArrayList<>();
        try (Response response = httpClient.newCall(request).execute()) {
            JSONArray jsonArray = JSON.parseArray(response.body().string());
            for(int i = 0; i< jsonArray.size(); i++){
                courseFileList.add(new CourseFile(jsonArray.getJSONObject(i)));
            }
            return courseFileList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}