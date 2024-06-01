package com.example.smile.fileViewer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smile.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PDFViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_viewer);
        Intent intent = getIntent();
        String file_path = intent.getStringExtra("pdf_file");
        File file = new File(file_path);
        PDFView pdfView = findViewById(R.id.pdf_view);
        pdfView.fromFile(file)
                //                .pages(0, 2, 1, 3, 3, 3) //限制能显示的页面为那些页，不写的话默认展示所有页面，写的话就只会展示你写的哪些页面，例如这行代码规定了只能展示0，2，1，3，3，3这六页。
                .enableSwipe(true) // 允许使用刷卡阻止更改页面
                .swipeHorizontal(false)//是否水平翻页，默认竖直翻页
                .enableDoubletap(false)//是否可以双击方法页面
                //.defaultPage(defaultPage)//打开时候的默认页面，这里是上面我自定义的第一页，建议链接数据库，数据放在数据库里好一些。
//                .onPageChange(new OnPageChangeListener() {//设置翻页监听
//                    @Override
//                    public void onPageChanged(int page, int pageCount) {
//                        //这里面写当监听器监听到对应改变是的反应
//                        //例如展示数据与处理数据。
//                        //Toast.makeText(MainActivity.this, page + " / " + pageCount, Toast.LENGTH_SHORT).show();
//                        }
//                    }
                //.onDraw(onDrawListener) //允许借鉴的东西当前页面，通常在屏幕中间可见
                //.onDrawAll(onDrawListener)//允许在所有页面上分别为每个页面绘制内容。仅针对可见页面调用
                //.onLoad(onLoadCompleteListener) // 在文档加载并开始呈现之后.设置加载监听
                //.onPageScroll(onPageScrollListener)//设置页面滑动监听
                //.onError(onErrorListener)
                //.onPageError(onPageErrorListener)
                //.onRender(onRenderListener) //首次呈现文档后,首次提交文档后调用。
                //调用轻按一次即可返回true（如果已处理），则返回false以切换滚动柄可见性
                //.onTap(onTapListener)
                //.onLongPress(onLongPressListener)
                .enableAnnotationRendering(true)//呈现注释（例如注释，颜色或表单）
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true)//改善低分辨率屏幕上的渲染
                // dp中页面之间的间距。以限定间隔颜色，组视图背景
                .spacing(0)
                //.autoSpacing(false) //添加动态间距以适合在屏幕上在其自己的每一页
                //.linkHandler(DefaultLinkHandler)
//                .pageFitPolicy(FitPolicy.WIDTH) //模式，以适应视图中的页面
//                .fitEachPage(false) //使每个页面适合视图，否则较小页面相对于最大页面缩放。
//                .pageSnap(false) //将页面捕捉到屏幕边界
//                .pageFling(false) //仅更改单个页面，例如ViewPager
//                .nightMode(false) //切换夜间模式
                .enableSwipe(true)///是否允许翻页，默认是允许翻
                .onPageChange(null)//当pdf翻页 / 当阅读页数改变时
                .load();//开始加载pdf文件
    }
}
