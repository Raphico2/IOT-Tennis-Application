package com.example.tutorial6;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.data.Entry;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MenuFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected {False, Pending, True}

    private Connected connected = Connected.False;
    private String deviceAddress;
    private SerialService service;
    private boolean initialStart = true;
    private TextView receiveText;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;

    boolean StartFlag = false;
    boolean StopFlag = false;
    private String sport_activity = "run";
    private TextView timerTextView;
    private String newline = TextUtil.newline_crlf;
    private long startTime;

    ArrayList<String[]> rows = new ArrayList<>();

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            updateTimer(elapsedTime);
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");

    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    //button of the page
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu, container, false);

        timerTextView = rootView.findViewById(R.id.timerTextView);
        receiveText = rootView.findViewById(R.id.bluetooth_conexion_text);
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        Switch buttonSwitch = (Switch) rootView.findViewById(R.id.switchToggle);
        Button buttonStart = (Button) rootView.findViewById(R.id.Start_Button);
        Button buttonStop = (Button) rootView.findViewById(R.id.Stop_Button);
        ImageButton buttonStats = (ImageButton) rootView.findViewById(R.id.stat_button);
        ImageButton buttonProfile = (ImageButton) rootView.findViewById(R.id.ranking_button);
        Button buttonBack = (Button) rootView.findViewById(R.id.back_arrow);

        buttonSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sport_activity = "tennis";
                } else {
                    sport_activity = "run";
                }
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rows = new ArrayList<>();
                StartFlag = true;
                StopFlag = false;
                startTime = System.currentTimeMillis();
                handler.postDelayed(runnable, 1000);
                System.out.println(" Start pressed");
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartFlag = false;
                StopFlag = true;
                startTime = 0;
                handler.removeCallbacks(runnable);
                timerTextView.setText("00:00:00");
                writeToCsv(rows, sport_activity);
            }

        });

        buttonStats.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sport_activity.equals("run")) {
                    Intent intent = new Intent(getActivity(), RunStats.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                } else {
                    Intent intent = new Intent(getActivity(), TennisStats.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                }
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sport_activity.equals("run")) {
                    Intent intent = new Intent(getActivity(), RunProfile.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                } else {
                    Intent intent = new Intent(getActivity(), TennisProfile.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirection vers la page run_main
                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);

        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
            receive(data);
        } catch (Exception e) {
            System.out.println(" Exeption ");
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }


    private ArrayList<Entry> emptyDataValues() {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        return dataVals;
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    private void updateTimer(long elapsedTime) {
        String timerText = formatTime(elapsedTime);
        timerTextView.setText(timerText);
    }

    private String formatTime(long millis) {
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((millis % (1000 * 60)) / 1000);

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    //write to csv&
    private void writeToCsv(ArrayList<String[]> rows, String sport_activity) {
        try {

            File file = new File("/storage/emulated/0/Download/memory.csv");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(new FileWriter(file, false));
            String row0[] = new String[]{sport_activity};
            csvWriter.writeNext(row0);
            String row1[] = new String[]{"Time", "Ax", "Ay", "Az", "Gx", "Gy", "Gz", "Mx", "My", "Mz", "ANorm", "GNorm", "MNorm"};
            csvWriter.writeNext(row1);
            for (int i = 0; i < rows.size(); i++) {
                csvWriter.writeNext(rows.get(i));
            }
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receive(byte[] message) {  // This part gets the Arduino reading

        if (hexEnabled) {
            receiveText.append(TextUtil.toHexString(message) + '\n');
        } else {
            if (StopFlag) {
            }
            if (StartFlag) {
                // All the stuff in receive() goes here
                String msg = new String(message);
                if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                    // don't show CR as ^M if directly before LF
                    String msg_to_save = msg;
                    msg_to_save = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
                    // check message length
                    if (msg_to_save.length() > 1) {
                        // split message string by ',' char
                        String[] parts = msg_to_save.split(",");
                        // function to trim blank spaces
                        parts = clean_str(parts);

                        String row[] = new String[]{parts[12], parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9], parts[10], parts[11]};

                        rows.add(row);

                    }

                    msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                    // send msg to function that saves it to csv
                    // special handling if CR and LF come in separate fragments
                    if (pendingNewline && msg.charAt(0) == '\n') {
                        Editable edt = receiveText.getEditableText();
                        if (edt != null && edt.length() > 1)
                            edt.replace(edt.length() - 2, edt.length(), "");
                    }
                    pendingNewline = msg.charAt(msg.length() - 1) == '\r';
                }
                receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
            }

        }
    }

    private String[] clean_str(String[] stringsArr) {
        for (int i = 0; i < stringsArr.length; i++) {
            stringsArr[i] = stringsArr[i].replaceAll(" ", "");
        }
        return stringsArr;
    }
}
