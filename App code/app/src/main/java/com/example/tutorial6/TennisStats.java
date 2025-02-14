package com.example.tutorial6;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import java.util.Locale;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TennisStats extends AppCompatActivity{
    private EditText TextStroke_Number;
    private EditText TextForehand;
    private EditText TextBackhand;
    private EditText TextMaximumSpeed;
    private EditText TextAverageSpeed;
    private EditText TextMaxRally;
    private EditText TextAverageRally;
    private Button backButton;


   //Button and stats here
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_tennis);

        TextStroke_Number= findViewById(R.id.display_strike_number);
        TextForehand = findViewById(R.id.display_forehand_number);
        TextBackhand = findViewById(R.id.display_backhand_number);
        TextMaximumSpeed = findViewById(R.id.display_max_speed);
        TextAverageSpeed = findViewById(R.id.display_mean_speed);
        TextMaxRally = findViewById(R.id.display_max_exchange);
        TextAverageRally = findViewById(R.id.display_mean_exchange);
        backButton = (Button) findViewById(R.id.back_arrow);

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        Python py =  Python.getInstance();
        PyObject pyobj = py.getModule("run");
        PyObject obj = pyobj.callAttr("main");

        String result_python_call = obj.toString();
        String[] result = result_python_call.split(";\\s*");

        System.out.println(result[0]);
        System.out.println(result[1]);
        System.out.println(result[2]);
        System.out.println(result[3]);
        System.out.println(result[4]);
        System.out.println(result[5]);
        System.out.println(result[6]);


        String stroke_number = result[0];
        String number_backhand = result[1];
        String number_forehand = result[2];
        String maximum_speed = result[4];
        String average_speed = result[3];
        String maximum_rally = result[5];
        String average_rally = result[6];

        if (TextStroke_Number != null) {
            TextStroke_Number.setText(stroke_number);
        }
        if (TextForehand != null) {
            TextForehand.setText(number_forehand);
        }
        if (TextBackhand != null) {
            TextBackhand.setText(number_backhand);
        }
        if (TextMaximumSpeed != null) {
            TextMaximumSpeed.setText(maximum_speed);
        }
        if (TextAverageRally != null) {
            TextAverageRally.setText(average_rally);
        }
        if (TextAverageSpeed != null) {
            TextAverageSpeed.setText(average_speed);
        }
        if (TextMaxRally != null) {
            TextMaxRally.setText(maximum_rally);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                    onBackPressed();
        }});

    }

}
