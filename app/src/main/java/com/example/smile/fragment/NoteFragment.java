package com.example.smile.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.example.smile.R;
import com.example.smile.activity.MainActivity;
import com.example.smile.entity.Elearning.AssignmentEntity;
import com.example.smile.entity.Elearning.CourseEntity;
import com.example.smile.entity.Elearning.SerializableHashMap;
import com.example.smile.util.DensityUtil;
import com.example.smile.util.FileUtil;
import com.example.smile.view.CustomPopWindow;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoteFragment extends Fragment {
    private final String[] title={"课程","作业","文件"};

    private List<CourseEntity> items;
    private HashMap<Integer, List<AssignmentEntity>> child_items;

    boolean lock = false;

    void data_persistence_read(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(lock) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                lock = true;
                String items_String  = null;
                if(FileUtil.fileExists(MainActivity.getInstance(),"note_items.txt")){
                    items_String = FileUtil.readFile(MainActivity.getInstance(),"note_items.txt");
                }
                if(items_String != null) {
                    items = JSON.parseArray(items_String, CourseEntity.class);
                }

                String child_items_String = null;
                if(FileUtil.fileExists(MainActivity.getInstance(),"note_child_items.txt")){
                    child_items_String = FileUtil.readFile(MainActivity.getInstance(),"note_child_items.txt");
                }
                if(child_items_String != null) {
                    child_items = (HashMap<Integer, List<AssignmentEntity>>)JSON.parseObject(child_items_String, new TypeReference<HashMap<Integer, List<AssignmentEntity>>>(){});
                }
                elearningHw.setItems(items,child_items,false);
                elearningCourse.setItems(items);
                lock = false;
            }
        }).start();

    }

    void data_persistence_write(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(getActivity()!=null) {
                    while (lock) ;
                    lock = true;
                    FileUtil.coverAndWrite(MainActivity.getInstance(), "note_items.txt", JSON.toJSONString(items));
                    FileUtil.coverAndWrite(MainActivity.getInstance(), "note_child_items.txt", JSON.toJSONString(child_items));
                    lock = false;
                }
            }
        }).start();

    }

    Fragment[] fragments;
    NoteListFragment elearningHw;
    NoteFileFragment elearningFile;
    NoteCourseFragment elearningCourse;
    public static String NOTE_BROADCAST_FLAG = "note.broadcast.flag";

    class NoteBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            items = (List<CourseEntity>) intent.getSerializableExtra("courseList");
            child_items = ((SerializableHashMap)intent.getSerializableExtra("assignmentList")).getMap();
            elearningHw.setItems(items,child_items,true);
            elearningCourse.setItems(items);
            data_persistence_write();
        }
    }

    public NoteBroadCastReceiver getBoadCastReceiver(){
        return new NoteBroadCastReceiver();
    }
    ActivityResultLauncher<Intent> selectImage;
    ActivityResultLauncher<Intent> resizeAndSetImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("debug","NoteFragment_onCreate");
        super.onCreate(savedInstanceState);
        elearningHw = new NoteListFragment();
        elearningFile = new NoteFileFragment();
        elearningCourse = new NoteCourseFragment();
        items = new ArrayList<>();
        child_items = new HashMap<>();
        data_persistence_read();


        selectImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                    Uri selectedImage = result.getData().getData(); //获取系统返回的照片的Uri
                    resizeImage(selectedImage);
                } else {
                    Toast.makeText(getContext(), "操作失败", Toast.LENGTH_LONG).show();
                }

            }
        });

        resizeAndSetImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getData()==null) return;
                Bitmap photo = result.getData().getParcelableExtra("data");
                elearning_user_image.setImageBitmap(photo);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtil.writeBitmapFile(getContext(),"user_image", photo);
                    }
                }).start();
            }
        });


    }

    RoundedImageView elearning_user_image;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("debug","NoteFragment_onCreateView");
        View view=inflater.inflate(R.layout.note_main,container,false);
        //保证getActivity()可以获取到上下文
        if(getActivity()==null || getContext()==null){
            Log.e("???","???");
            return view;
        }
        elearning_user_image = view.findViewById(R.id.user_image);
        if(FileUtil.fileExists(MainActivity.getInstance(),"user_image")){
            elearning_user_image.setImageBitmap(FileUtil.readBitmapFile(MainActivity.getInstance(),"user_image"));
        }

        TabLayout tlCheckTitle = view.findViewById(R.id.tabLayout);
        ViewPager2 vpCheckContainer = view.findViewById(R.id.note_view_pager);
        //实例化适配器
        MYFragmentStateAdapter myFragmentStateAdapter=new MYFragmentStateAdapter(getActivity());
        vpCheckContainer.setAdapter(myFragmentStateAdapter);
        vpCheckContainer.setOffscreenPageLimit(1);
        //TabLayout与ViewPager2关联
        new TabLayoutMediator(tlCheckTitle, vpCheckContainer, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(title[position]);
            }
        }).attach();//不要忘记 否则没效果

        elearning_user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPopupMenu();
            }
        });
        return view;
    }

    void userPopupMenu(){
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.note_user_menu, null);
        CustomPopWindow mCustomPopWindow = new CustomPopWindow.PopupWindowBuilder(getContext())
                .setView(contentView)
                .setFocusable(true)
                .enableBackgroundDark(false)
                .setAnimationStyle(R.style.CustomPopWindowStyle)
                .create();
        handleMenuLogic(mCustomPopWindow, contentView);
        mCustomPopWindow.showAsDropDown(elearning_user_image,0,0);

    }

    static Integer IMAGE_REQUEST_CODE = 0;


    void handleMenuLogic(CustomPopWindow mCustomPopWindow, View contentView){
        contentView.findViewById(R.id.note_user_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCustomPopWindow.dissmiss();
                elearningHw.load();
            }
        });

        contentView.findViewById(R.id.note_user_sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCustomPopWindow.dissmiss();
                SharedPreferences e_load = getContext().getSharedPreferences("elearning_load", MODE_PRIVATE);
                e_load.edit().clear().apply();
                items = null;
                child_items = null;
                elearningHw.setItems(null, null,false);
                elearningCourse.setItems(null);
            }
        });

        contentView.findViewById(R.id.note_user_change_image).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View view) {
                mCustomPopWindow.dissmiss();
                Intent intent = new Intent(Intent.ACTION_PICK,null);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                selectImage.launch(intent);
            }
        });


    }

    public void resizeImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //裁剪的大小
        intent.putExtra("outputX", DensityUtil.dp2px(getContext(),50));
        intent.putExtra("outputY", DensityUtil.dp2px(getContext(),50));
        intent.putExtra("return-data", true);
        resizeAndSetImage.launch(intent);
    }


    //内部适配器
    class  MYFragmentStateAdapter extends FragmentStateAdapter {
        //存放Fragment

        public MYFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragments=new Fragment[title.length];
        }

        //创建Fragment
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (fragments[position]==null){
                //创建Fragment
                //fragments[0]=elearningCourse;
                fragments[0]=elearningCourse;
                fragments[1]=elearningHw;
                fragments[2]=elearningFile;
            }
            return fragments[position];
        }
        //获取Fragment的数量
        @Override
        public int getItemCount() {
            return fragments.length;
        }
    }

    @Override
    public void onDestroy() {
        data_persistence_write();
        super.onDestroy();
    }
}

