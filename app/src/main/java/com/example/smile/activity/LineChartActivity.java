package com.example.smile.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smile.R;
import com.example.smile.adapter.TodoContentRecycleAdapter;
import com.example.smile.adapter.TodoGraphAdapter;
import com.example.smile.constants.Constants;
import com.example.smile.dao.TodoDao;
import com.example.smile.dao.TodoTimeDao;
import com.example.smile.entity.TodoEntity;
import com.example.smile.util.TimeUtil;
import com.example.smile.view.DynamicLineChartMarkView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class LineChartActivity extends AppCompatActivity{
    public static final int MSG_START = 1; // handler消息，开始添加点

    // 曲线编号
    public static final int LINE_NUMBER_1 = 0;
    public static final int LINE_NUMBER_2 = 1;
    public static final int LINE_NUMBER_3 = 2;
    public static final int LINE_NUMBER_4 = 3;



    private final Random mRandom = new Random(); // 随机产生点
    private final DecimalFormat mDecimalFormat = new DecimalFormat("#.00");   // 格式化浮点数位两位小数
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat df = new SimpleDateFormat("mm:ss");//设置日期格式
    private final Map<Integer,String> timeDate = new HashMap<Integer,String>();//存储x轴的时间
    private final Map<Integer,String> mineTime = new HashMap<Integer,String>();//存储x轴的时间

    LineChart mLineChart; // 曲线表，存线集合
    LineData mLineData; // 线集合，所有折现以数组的形式存到此集合中
    XAxis mXAxis; //X轴
    YAxis mLeftYAxis; //左侧Y轴
    YAxis mRightYAxis; //右侧Y轴
    Legend mLegend; //图例
    LimitLine mLimitline; //限制线
    TodoDao todoDao;
    TodoTimeDao todoTimeDao;
    TodoGraphAdapter todoGraphAdapter;
    RecyclerView recyclerView;


    // Chart需要的点数据链表
    List<Entry> mEntries1 = new ArrayList<>();
    List<Entry> mEntries2 = new ArrayList<>();
    List<Entry> mEntries3 = new ArrayList<>();
    List<Entry> mEntries4 = new ArrayList<>();
    // LineDataSet:点集合,即一条线
    LineDataSet mLineDataSet1 = new LineDataSet(mEntries1, "very_important");
    LineDataSet mLineDataSet2 = new LineDataSet(mEntries2, "important");
    LineDataSet mLineDataSet3 = new LineDataSet(mEntries3, "very_easy");
    LineDataSet mLineDataSet4 = new LineDataSet(mEntries4, "easy");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.line_chart);
        initView();
        initLineChart();
        initLineChartListener();
        //线条的渐变背景
        Drawable drawable1 = ContextCompat.getDrawable(this, R.drawable.fade_very_important);
        setChartFillDrawable(drawable1, mLineDataSet1);
        Drawable drawable2 = ContextCompat.getDrawable(this, R.drawable.fade_important);
        setChartFillDrawable(drawable2, mLineDataSet2);
        Drawable drawable3 = ContextCompat.getDrawable(this, R.drawable.fade_very_easy);
        setChartFillDrawable(drawable3, mLineDataSet3);
        Drawable drawable4 = ContextCompat.getDrawable(this, R.drawable.fade_easy);
        setChartFillDrawable(drawable4, mLineDataSet4);

        setValue();

    }


    public void initView() {
        todoDao = new TodoDao(this);
        todoTimeDao = new TodoTimeDao(todoDao);
        recyclerView = findViewById(R.id.chart_recylerview);
        todoGraphAdapter = new TodoGraphAdapter(this);
        TodoContentRecycleAdapter.OnItemClickListener onItemClickListener = new TodoContentRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TodoEntity entity = todoGraphAdapter.getDataBases().get(position);
                Intent intent = new Intent();
                intent.setClass(LineChartActivity.this, TodoContentActivity.class);
                intent.putExtra(TodoContentActivity.TODO_CONTENT_ENTITY, entity);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        };
        todoGraphAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(todoGraphAdapter);

    }

    void initLineChartListener(){
        mLineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String time = mineTime.get((int)e.getX());
                List<TodoEntity> list = new ArrayList<>();
                if(time!=null) list = todoTimeDao.getEntityList(time, 0, false);
                todoGraphAdapter.setDataBases(list);
            }

            @Override
            public void onNothingSelected() {

            }
        });




    }


    int max = 0;

    public int getCount(int value){
        if(value > max){max = value;}
        return value;
    }

    public void setValue(){
        List<String> timeXis = TimeUtil.findEveryDay(todoTimeDao.getMinTimeBackTime(), todoTimeDao.getMaxTimeBackTime());
        for(int i = 0; i < timeXis.size(); i++){
            @SuppressLint("SimpleDateFormat") SimpleDateFormat old_df = new SimpleDateFormat("yyyy MM-dd HH:mm");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MM月dd");
            String cur = df.format(new Date());
            try {
                String day = df.format(Objects.requireNonNull(old_df.parse(timeXis.get(i))));
                timeDate.put(i, day.equals(cur) ? "今天":day);
                mineTime.put(i,timeXis.get(i));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mLineChart.notifyDataSetChanged();
            mLineData.notifyDataChanged();

            addEntry(mLineData, i, getCount(todoTimeDao.getCount(timeXis.get(i), Constants.NICE.VERY_IMPORTANT)), LINE_NUMBER_1);
            addEntry(mLineData, i, getCount(todoTimeDao.getCount(timeXis.get(i), Constants.NICE.IMPORTANT)), LINE_NUMBER_2);
            addEntry(mLineData, i, getCount(todoTimeDao.getCount(timeXis.get(i), Constants.NICE.VERY_EASY)), LINE_NUMBER_3);
            addEntry(mLineData, i, getCount(todoTimeDao.getCount(timeXis.get(i), Constants.NICE.EASY)), LINE_NUMBER_4);

            mLineChart.notifyDataSetChanged();
            mLineData.notifyDataChanged();
            //把yValues移到指定索引的位置
            //mlineChart.moveViewToAnimated(xCount - 4, yValues, YAxis.AxisDependency.LEFT, 1000);// TODO: 2019/5/4 内存泄漏，异步 待修复

        }
        mXAxis.resetAxisMaximum();
        mXAxis.setAvoidFirstLastClipping(true);
        mLeftYAxis.setAxisMaximum(max + 10);
        mRightYAxis.setAxisMaximum(max + 10);
        mLineChart.setVisibleYRangeMaximum(max+ (int)(max/3), YAxis.AxisDependency.LEFT);// 当前统计图表中最多在Y轴坐标线上显示的总量
        mLineChart.setVisibleYRangeMinimum(max+1, YAxis.AxisDependency.LEFT);
        mLineChart.setVisibleYRangeMaximum(max + (int)(max/4), YAxis.AxisDependency.RIGHT);// 当前统计图表中最多在Y轴坐标线上显示的总量
        mLineChart.setVisibleYRangeMinimum(max+1, YAxis.AxisDependency.RIGHT);
        mLeftYAxis.setLabelCount(20);
        Description description = mLineChart.getDescription();
        description.setEnabled(false);
//        mLeftYAxis.setAxisMaximum(max + 10);
//        mRightYAxis.setPosition(0);
        mLineChart.moveViewTo(timeDate.size() - 4,0, YAxis.AxisDependency.LEFT);
        mLineChart.invalidate();


        //通知数据已经改变
        mLineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();

        //把yValues移到指定索引的位置
        //mLineChart.moveViewToAnimated(xCount - 4, yValues, YAxis.AxisDependency.LEFT, 1000);
        mLineChart.invalidate();
    }



    /**
     * 功能：初始化LineChart
     */
    public void initLineChart() {
        mLineChart = findViewById(R.id.line_chart);
        mXAxis = mLineChart.getXAxis(); // 得到x轴
        mLeftYAxis = mLineChart.getAxisLeft(); // 得到侧Y轴
        mRightYAxis = mLineChart.getAxisRight(); // 得到右侧Y轴
        mLegend = mLineChart.getLegend(); // 得到图例
        mLineData = new LineData();
        mLineChart.setData(mLineData);

        // 设置图标基本属性
        setChartBasicAttr(mLineChart);

        timeDate.put(0,df.format(System.currentTimeMillis()));
        // 设置XY轴
        setXYAxis(mLineChart, mXAxis, mLeftYAxis, mRightYAxis);
        mXAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return timeDate.get((int) value % timeDate.size());
            }
        });
        //mXAxis.setAxisMaximum(timeDate.size());
        //mXAxis.setLabelCount(timeDate.size());
        // 添加线条
        initLine();
        // 设置图例
        createLegend(mLegend);
        // 设置MarkerView
        //setMarkerView(mLineChart);
    }


    /**
     * 功能：设置图标的基本属性
     */
    public void setChartBasicAttr(LineChart lineChart) {
        /***图表设置***/
        lineChart.setDrawGridBackground(false); //是否展示网格线
        lineChart.setDrawBorders(true); //是否显示边界
        lineChart.setDragEnabled(true); //是否可以拖动
        lineChart.setScaleEnabled(true); // 是否可以缩放
        lineChart.setTouchEnabled(true); //是否有触摸事件
        //设置XY轴动画效果
        lineChart.animateY(500);
//        lineChart.animateX(500);
    }

    /**
     * 功能：设置XY轴
     */
    void setXYAxis(LineChart lineChart, XAxis xAxis, YAxis leftYAxis, YAxis rightYAxis) {
        /***XY轴的设置***/
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //X轴设置显示位置在底部
        xAxis.setAxisMinimum(0f); // 设置X轴的最小值
        xAxis.setAxisMaximum(30); // 设置X轴的最大值
        xAxis.setLabelCount(20, false); // 设置X轴的刻度数量，第二个参数表示是否平均分配
        xAxis.setGranularity(1f); // 设置X轴坐标之间的最小间隔

        lineChart.setVisibleXRangeMaximum(5);// 当前统计图表中最多在x轴坐标线上显示的总量
        lineChart.setVisibleXRangeMinimum(5);
        //保证Y轴从0开始，不然会上移一点
        leftYAxis.setAxisMinimum(0f);
        rightYAxis.setAxisMinimum(0f);
        leftYAxis.setAxisMaximum(100f);
        rightYAxis.setAxisMaximum(100f);
        leftYAxis.setGranularity(1f);
        rightYAxis.setGranularity(1f);
        leftYAxis.setLabelCount(10);
        lineChart.setVisibleYRangeMaximum(30, YAxis.AxisDependency.LEFT);// 当前统计图表中最多在Y轴坐标线上显示的总量
        lineChart.setVisibleYRangeMaximum(30, YAxis.AxisDependency.RIGHT);// 当前统计图表中最多在Y轴坐标线上显示的总量
        leftYAxis.setEnabled(false);

        //leftYAxis.setCenterAxisLabels(true);// 将轴标记居中
        // leftYAxis.setDrawZeroLine(true); // 原点处绘制 一条线
        //leftYAxis.setZeroLineColor(Color.RED);
        //leftYAxis.setZeroLineWidth(1f);
    }

    /**
     * 功能：对图表中的曲线初始化，添加三条，并且默认显示第一条
     */
    private void initLine() {

        createLine(mLineDataSet1, getColor(R.color.color_veryimportant), mLineData, mLineChart);
        createLine(mLineDataSet2, getColor(R.color.color_important), mLineData, mLineChart);
        createLine(mLineDataSet3, getColor(R.color.color_veryeasy), mLineData, mLineChart);
        createLine(mLineDataSet4, getColor(R.color.color_easy), mLineData, mLineChart);

        // mLineData.getDataSetCount() 总线条数
        // mLineData.getEntryCount() 总点数
        // mLineData.getDataSetByIndex(index).getEntryCount() 索引index处曲线的总点数
        // 每条曲线添加到mLineData后，从索引0处开始排列
//        for (int i = 0; i < mLineData.getDataSetCount(); i++) {
//            mLineChart.getLineData().getDataSets().get(i).setVisible(true); //
//        }
        showLine(LINE_NUMBER_1);
        showLine(LINE_NUMBER_2);
        showLine(LINE_NUMBER_3);
        showLine(LINE_NUMBER_4);
    }

    /**
     * 功能：根据索引显示或隐藏指定线条
     */
    public void showLine(int index) {
        mLineChart
                .getLineData()
                .getDataSets()
                .get(index)
                .setVisible(true);
        mLineChart.invalidate();
    }

    /**
     * 功能：动态创建一条曲线
     */
    private void createLine(LineDataSet lineDataSet, int color, LineData lineData, LineChart lineChart) {

        // 初始化线条
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);

        if (lineData == null) {
            lineData = new LineData();
            lineData.addDataSet(lineDataSet);
            lineChart.setData(lineData);
        } else {
            lineChart.getLineData().addDataSet(lineDataSet);
        }

        lineChart.invalidate();
    }


    /**
     * 曲线初始化设置,一个LineDataSet 代表一条曲线
     *
     * @param lineDataSet 线条
     * @param color       线条颜色
     * @param mode
     */
    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color); // 设置曲线颜色
        lineDataSet.setCircleColor(color);  // 设置数据点圆形的颜色
        lineDataSet.setDrawCircleHole(false);// 设置曲线值的圆点是否是空心
        lineDataSet.setLineWidth(2f); // 设置曲线宽度
        lineDataSet.setCircleRadius(3f); // 设置折现点圆点半径
        lineDataSet.setValueTextSize(10f);

        lineDataSet.setDrawFilled(true); //设置曲线图填充
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        if (mode == null) {
            //设置曲线展示为圆滑曲线（如果不设置则默认曲线）
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }

    }


    /**
     * 功能：创建图例
     */
    private void createLegend(Legend legend) {
        /***曲线图例 标签 设置***/
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }


    /**
     * 设置线条填充背景颜色
     *
     * @param drawable
     */
    public void setChartFillDrawable(Drawable drawable, LineDataSet lineDataSet) {
        //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(drawable);
        mLineChart.invalidate();
    }


    /**
     * 设置 可以显示X Y 轴自定义值的 MarkerView
     */
    public void setMarkerView(LineChart lineChart) {
        DynamicLineChartMarkView mv = new DynamicLineChartMarkView(this);
        mv.setChartView(lineChart);
        lineChart.setMarker(mv);
        lineChart.invalidate();
    }



    public void addEntry(LineData lineData, int xValue, float yValues, int index){
        Entry entry = new Entry(xValue, yValues); // 创建一个点
        lineData.addEntry(entry, index); // 将entry添加到指定索引处的曲线中
    }
    /**
     * 动态添加数据
     * 在一个LineChart中存放的曲线，其实是以索引从0开始编号的
     *
     * @param yValues y值
     */
    public void addEntry(LineData lineData, LineChart lineChart, float yValues, int index) {
        // 通过索引得到一条曲线，之后得到曲线上当前点的数量
        int xCount = lineData.getDataSetByIndex(index).getEntryCount();
        //添加时间数据
         SimpleDateFormat df = new SimpleDateFormat("mm:ss");
        timeDate.put(xCount, df.format(System.currentTimeMillis()));


        Entry entry = new Entry(lineData.getEntryCount(), yValues); // 创建一个点
        lineData.addEntry(entry, index); // 将entry添加到指定索引处的曲线中
        Log.d("LineChartDynamicActivity", "addEntry: "+entry.getX());
        //通知数据已经改变
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();

        //把yValues移到指定索引的位置
//        lineChart.moveViewToAnimated(xCount - 4, yValues, YAxis.AxisDependency.LEFT, 1000);
        lineChart.invalidate();
//        int xCount = lineData.getDataSetByIndex(index).getEntryCount();
//        //添加时间数据
//        timeDate.put(xCount, df.format(System.currentTimeMillis()));
//
//        Entry entry = new Entry(xCount, yValues); // 创建一个点
//        lineData.addEntry(entry, index); // 将entry添加到指定索引处的曲线中
//
//        //通知数据已经改变
//        lineData.notifyDataChanged();
//        lineChart.notifyDataSetChanged();
//
//        //把yValues移到指定索引的位置
//        lineChart.moveViewToAnimated(xCount - 4, yValues, YAxis.AxisDependency.LEFT, 1000);// TODO: 2019/5/4 内存泄漏，异步 待修复
//        lineChart.invalidate();
    }




    /**
     * 功能：第1条曲线添加一个点
     */
    public void addLine1Data(float yValues) {
        addEntry(mLineData, mLineChart, yValues, LINE_NUMBER_1);
    }

    /**
     * 功能：第2条曲线添加一个点
     */
    public void addLine2Data(float yValues) {
        addEntry(mLineData, mLineChart, yValues, LINE_NUMBER_2);
    }

    /**
     * 功能：第3条曲线添加一个点
     */
    public void addLine3Data(float yValues) {
        addEntry(mLineData, mLineChart, yValues, LINE_NUMBER_3);
    }

    public void addLine4Data(float yValues) {
        addEntry(mLineData, mLineChart, yValues, LINE_NUMBER_4);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清空消息

        // moveViewToAnimated 移动到某个点，有内存泄漏，暂未修复，希望网友可以指着
        mLineChart.clearAllViewportJobs();
        mLineChart.removeAllViewsInLayout();
        mLineChart.removeAllViews();
    }

}