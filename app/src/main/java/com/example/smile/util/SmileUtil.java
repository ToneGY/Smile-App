package com.example.smile.util;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson2.JSON;
import com.example.smile.R;
import com.example.smile.activity.MainActivity;
import com.example.smile.entity.GroupEntity;
import com.example.smile.entity.ServerTodoEntity;
import com.example.smile.entity.TodoEntity;
import com.example.smile.entity.UserEntity;
import com.example.smile.util.Cookie.PersistentCookieStore;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class SmileUtil {
    public static OkHttpClient httpClient;
    public static String root_url = "http://192.168.0.109:8000";
    public static final MediaType JSONTYPE
            = MediaType.get("application/json; charset=utf-8");
    public static void init() {
        httpClient = new OkHttpClient();
    }

    public Request getRequest(String url){
        return new Request.Builder().url(url).get().build();
    }

    public static Request putRequest(String url, Object obj){
        Log.e("json",JSON.toJSONString(obj));
        return new Request.Builder().addHeader("content-type", "application/json").url(url).put(RequestBody.create(JSON.toJSONString(obj),JSONTYPE)).build();
    }

    public static Request postRequest(String url, Object obj){
        Log.e("json",JSON.toJSONString(obj));
        return new Request.Builder().addHeader("content-type", "application/json").url(url).post(RequestBody.create(JSON.toJSONString(obj),JSONTYPE)).build();
    }

    public static boolean register(String username, String account, String passwd){
        init();
        String url = root_url + "/user/register";
        UserEntity userEntity = new UserEntity();
        userEntity.setName(username);
        userEntity.setAccount(account);
        userEntity.setPasswd(passwd);
        userEntity.setId(0);


        Log.e("url",url);
        Log.e("json",JSON.toJSONString(userEntity));
        String json = JSON.toJSONString(userEntity);
        RequestBody requestBody = RequestBody.create(json, JSONTYPE);
        Request  request = new Request.Builder().post(requestBody).url(url).build();
        try{
            Response response = httpClient.newCall(request).execute();
            String body = response.body().string();
            Log.e("body",body);
            return !body.equals("fail");
        }catch (IOException e){
            e.printStackTrace();
            Log.e("报错:", e.toString());
            Log.e("fail","fail");
        }
        return false;
    }

    public static UserEntity login(String account, String passwd){
        init();
        String url = root_url + "/user/login/"+account+"?passwd="+passwd;
        Request request = new Request.Builder().get().url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
            return JSON.parseObject(response.body().string(),UserEntity.class);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getUserNameByAccount(String account){
        init();
        String url = root_url + "/user/get_username_by_account/"+account;
        Request request = new Request.Builder().get().url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
            return response.body().string();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void goupAddUser(int group_id, String account){
        init();
        String url = root_url + "/user/adduser/"+group_id+"?account="+account;
        Request request = new Request.Builder().get().url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
        }catch (IOException e){
            logExcepion(e);
        }
    }

    public static void groupDeleteUser(int group_id, String account){
        String url = root_url +"/user/delete_user_from_group";
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("group_id", String.valueOf(group_id));
        formBuilder.add("user_account",  account);
        Request request = new Request.Builder().delete(formBuilder.build()).url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
        }catch (IOException e){
            logExcepion(e);
        }
    }

    public static List<GroupEntity> getGroupByUser(String user_accout){
        init();
        String url = root_url + "/user/get_group_by_user/" + user_accout;
        Request request = new Request.Builder().get().url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
            if(response.body() == null) return null;
            String body = Objects.requireNonNull(response.body()).string();
            if(body.equals("") || body.equals("[]")) return null;
            Log.e("group",body);
            return JSON.parseArray(body, GroupEntity.class);
        } catch (IOException e) {
            logExcepion(e);
        }
        return null;
    }

    public static List<GroupEntity> addGroupByUser(String group_name, String user_account){
        init();
        String url = root_url + "/user/group_add";
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("group_name", group_name);
        builder.add("user_account", user_account);
        Request request = new Request.Builder().post(builder.build()).url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
            if(response.body() == null) return null;
            String body = Objects.requireNonNull(response.body()).string();
            if(body.equals("") || body.equals("[]")) return null;
            Log.e("group",body);
            return JSON.parseArray(body, GroupEntity.class);
        } catch (IOException e) {
            logExcepion(e);
        }
        return null;
    }

    public static List<UserEntity> getUserByGroupId(int group_id){
        init();
        String url = root_url + "/user/get_member_by_group_id/"+ group_id;
        FormBody.Builder builder = new FormBody.Builder();
        Request request = new Request.Builder().get().url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
            if(response.body() == null) return null;
            String body = Objects.requireNonNull(response.body()).string();
            if(body.equals("") || body.equals("[]")) return null;
            Log.e("group",body);
            return JSON.parseArray(body, UserEntity.class);
        } catch (IOException e) {
            logExcepion(e);
        }
        return null;
    }

    public static List<ServerTodoEntity> getTodoByGroupId(int group_id){
        init();
        String url = root_url + "/todo/get_todo_by_group_id/"+ group_id;
        FormBody.Builder builder = new FormBody.Builder();
        Request request = new Request.Builder().get().url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
            if(response.body() == null) return null;
            String body = Objects.requireNonNull(response.body()).string();
            if(body.equals("") || body.equals("[]")) return null;
            Log.e("group",body);
            return JSON.parseArray(body, ServerTodoEntity.class);
        } catch (IOException e) {
            logExcepion(e);
        }
        return null;
    }

    public static void addTodoEntity(ServerTodoEntity serverTodoEntity){
        init();
        String url = root_url + "/todo/add_todo_entity";
        String json = JSON.toJSONString(serverTodoEntity);
        Request request = new Request.Builder().post(RequestBody.create(json,JSONTYPE)).url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
        } catch (IOException e) {
            logExcepion(e);
        }
    }

    public static void updateTodoEntity(ServerTodoEntity serverTodoEntity){
        init();
        String url = root_url + "/todo/update_todo_entity";
        String json = JSON.toJSONString(serverTodoEntity);
        Request request = new Request.Builder().post(RequestBody.create(json,JSONTYPE)).url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
        } catch (IOException e) {
            logExcepion(e);
        }
    }

    public static void deleteTodoEntities(List<Integer> ids){
        init();
        String url = root_url + "/todo/delete_todo_entities";
        String json = JSON.toJSONString(ids);
        Log.e("com",json+ids.toString());
        Request request = new Request.Builder().delete(RequestBody.create(json,JSONTYPE)).url(url).build();
        try(Response response = httpClient.newCall(request).execute()){
        } catch (IOException e) {
            logExcepion(e);
        }
    }

    public static void logExcepion(Exception e){
        Log.e("error", e.toString());
    }
}
