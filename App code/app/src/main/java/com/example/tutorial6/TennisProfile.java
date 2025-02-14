package com.example.tutorial6;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;


import java.util.ArrayList;


public class TennisProfile extends AppCompatActivity{

    String[] variableNames = {"Consistency", "Technique", "Endurance", "Forehand power", "Backhand power"};
    EditText sentenceProfile;
    BarChart barChart;
    Button backButton;

    //Button and stats here
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_tennis);
        sentenceProfile = findViewById(R.id.text_profile);
        barChart = findViewById(R.id.bar_chart);
        backButton = (Button) findViewById(R.id.back_arrow);


        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        Python py =  Python.getInstance();
        PyObject pyobj = py.getModule("run");
        PyObject obj = pyobj.callAttr("main");

        String result_python_call = obj.toString();
        String[] result = result_python_call.split(";\\s*");

        List<BarEntry> listeFloats = new ArrayList<>();

        listeFloats.add(new BarEntry(0, Float.parseFloat(result[7])));
        listeFloats.add(new BarEntry(1, Float.parseFloat(result[8])));
        listeFloats.add(new BarEntry(2, Float.parseFloat(result[9])));
        listeFloats.add(new BarEntry(3, Float.parseFloat(result[10])));
        listeFloats.add(new BarEntry(4, Float.parseFloat(result[11])));

        String sentence = result[12];

        BarDataSet dataSet = new BarDataSet(listeFloats, "Data Set");
        dataSet.setColor(getResources().getColor(R.color.light_blue)); // Couleur des barres
        dataSet.setValueTextColor(getResources().getColor(R.color.black)); // Couleur du texte des valeurs
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        // Créer les données BarData avec le seul ensemble de données
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);

        // Configurer le graphique BarChart
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setHighlightPerTapEnabled(false);
        barChart.setHighlightPerDragEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setLabelCount(variableNames.length);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(variableNames)); // Utiliser les noms des variables pour l'axe X
        xAxis.setTextColor(getResources().getColor(R.color.black)); // Couleur des noms de variables
        xAxis.setLabelRotationAngle(45f);
        xAxis.setCenterAxisLabels(true);

        // Configurer l'axe Y
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(true);
        yAxis.setTextSize(10f);
        yAxis.setTextColor(getResources().getColor(R.color.black)); // Couleur des valeurs de l'axe Y

        barChart.getAxisRight().setEnabled(false);

        // Mettre à jour le graphique
        barChart.invalidate();

        if (sentenceProfile != null) {
            sentenceProfile.setText(sentence);
        }


        //buttonBack
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                onBackPressed();
            }});


       }

}




