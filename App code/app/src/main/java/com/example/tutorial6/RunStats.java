package com.example.tutorial6;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import com.opencsv.CSVWriter;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class RunStats extends AppCompatActivity{

    private EditText TextType;
    private EditText TextSpeed;
    private EditText TextNumber_step ;
    private EditText TextDistance;
    private Button backButton;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_run);
        backButton = (Button) findViewById(R.id.back_arrow);
        TextType = findViewById(R.id.display_type_activity);
        TextSpeed = findViewById(R.id.display_average_speed);
        TextNumber_step = findViewById(R.id.display_step_number);
        TextDistance = findViewById(R.id.display_distance_covered);


        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        Python py =  Python.getInstance();
        PyObject pyobj = py.getModule("run");
        PyObject obj = pyobj.callAttr("main");

        String result_python_call = obj.toString();
        String[] result = result_python_call.split(";\\s*");

        String type = result[0];
        String speed = result[1];
        String number_step = result[2];
        String distance = result[3];

        System.out.println(type);
        System.out.println(speed);
        System.out.println(number_step);
        System.out.println(distance);

        if (TextType != null) {
            TextType.setText(type);
        }

        if (TextSpeed != null) {
            TextSpeed.setText(speed);
        }

        if (TextDistance != null) {
            TextDistance.setText(distance);
        }

        if (TextNumber_step != null) {
            TextNumber_step.setText(number_step);
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

}
