package com.example.smile.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smile.R;
import com.example.smile.dao.TodoDao;
import com.example.smile.dao.TodoTimeDao;
import com.example.smile.util.TimeUtil;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChartViewActivity extends AppCompatActivity {
    BubbleChart chart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.char_view);
        chart = findViewById(R.id.id_barchart);
//        List<BarEntry> entries = new ArrayList<>();
        List<BubbleEntry> entriesGroup1 = new ArrayList<>();
        List<BubbleEntry> entriesGroup2 = new ArrayList<>();
// fill the lists
        entriesGroup1.add(new BubbleEntry(0f, 30f,30f));
        entriesGroup1.add(new BubbleEntry(1f, 8,8));
        entriesGroup1.add(new BubbleEntry(2f,  6,6));
        entriesGroup1.add(new BubbleEntry(3f,  51,51));
        entriesGroup1.add(new BubbleEntry(4f,  2,2));
        entriesGroup1.add(new BubbleEntry(5f,  21,21));
        entriesGroup1.add(new BubbleEntry(6f, 6,6));


        BubbleDataSet set1 = new BubbleDataSet(entriesGroup1, "Group 1");
        set1.setColors(getResources().getColor(R.color.color_veryimportant,null),getResources().getColor(R.color.color_important,null),getResources().getColor(R.color.color_easy,null),getResources().getColor(R.color.color_veryeasy,null));
        ArrayList<IBubbleDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
// (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"
        BubbleData data = new BubbleData(dataSets);
        //data.setBarWidth(0.65f); // set the width of each bar

        data.setValueFormatter(new LargeValueFormatter());


        chart.setBackgroundColor(Color.rgb(255,255,255));
        chart.setData(data);
        //chart.setFitBars(true); // make the x-axis fit exactly all bars
        chart.setBorderWidth(1);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getAxisLeft().setInverted(false);
        chart.getAxisRight().setEnabled(false);
        //chart.setDrawBarShadow(true);
        //chart.setDrawValueAboveBar(false);
        //chart.setFitBars(false);
        //.setHighlightFullBarEnabled(true);
//        chart.groupBars(0f, 0.06f, 0.02f); // perform the "explicit" grouping
        chart.invalidate(); // refresh

        Description description = chart.getDescription();
        description.setEnabled(false);

        chart.setScaleYEnabled(false);
        chart.setDoubleTapToZoomEnabled(true);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);


        YAxis left = chart.getAxisLeft();
        left.setDrawLimitLinesBehindData(true);
        left.setDrawLabels(true); // no axis labels
        left.setDrawAxisLine(true); // no axis line
        left.setDrawGridLines(true); // no grid lines
        left.setDrawZeroLine(false); // draw a zero line
        left.setTextColor(Color.BLACK);
        left.setGranularity(10); // interval 1
        //left.setLabelCount(8, true); // force 6 labels

        XAxis top = chart.getXAxis();
        top.setDrawLimitLinesBehindData(true);

        top.setDrawAxisLine(false);
        top.setDrawGridLines(false);

        TodoDao todoDao = new TodoDao(ChartViewActivity.this);
        TodoTimeDao todoTimeDao = new TodoTimeDao(todoDao);
        List<String> dateList = TimeUtil.findEveryDay(todoTimeDao.getMinTimeBackTime(),todoTimeDao.getMaxTimeBackTime());
        top.setValueFormatter(new IndexAxisValueFormatter() {

            @Override
            public String[] getValues() {
                return (String[]) dateList.toArray();
            }
        });
        top.setLabelCount(dateList.size());
        top.setPosition(XAxis.XAxisPosition.BOTTOM);





//        entries.add(new BarEntry(0f, 30f));
//        entries.add(new BarEntry(1f, 80f));
//        entries.add(new BarEntry(2f, 60f));
//        entries.add(new BarEntry(3f, 50f));
//        // gap of 2f
//        entries.add(new BarEntry(5f, 70f));
//        entries.add(new BarEntry(6f, 60f));
//        BarDataSet set = new BarDataSet(entries, "BarDataSet");
//
//        BarData data = new BarData(set);
//        data.setBarWidth(0.9f); // set custom bar width
//        chart.setData(data);
//        chart.setFitBars(true); // make the x-axis fit exactly all bars
//        chart.invalidate(); // refresh
    }

}
