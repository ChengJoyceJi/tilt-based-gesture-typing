package com.example.joyce.myapplication;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    EditText mEditText;
    Button mButton;
    Button mButton2;
    Button mClearButton;
    Button mStartButton;
    TextView infoip, msg;
    View mRelativeLayout;
    LinearLayout mMainContainer;

    private PopupWindow mPopupWindow;

    Server server;

    float[] gyroData = new float[] {0, 0, 0};
    int indicator = 1;

    // Pose 1
//    float[] ARDUINO_RECT = new float[] {15, -20, 20, -15}; // small
//    float[] ARDUINO_RECT = new float[] {20, -45, 45, -20}; // medium
   float[] ARDUINO_RECT = new float[] {30, -50, 50, -30}; // use this
//    float[] ARDUINO_RECT = new float[] {30, -60, 60, -30}; // large

    // Pose 2
//    float[] ARDUINO_RECT = new float[] {30, -30, 20, -20}; // small
//    float[] ARDUINO_RECT = new float[] {45, -45, 30, -30}; // medium
//   float[] ARDUINO_RECT = new float[] {50, -50, 35, -35}; // use this
//    float[] ARDUINO_RECT = new float[] {60, -60, 45, -45}; // large


//    // Full screen
//    float[] CHATHEAD_SELECTWORD_RECT = new float[] {0, 750, 670, 1000};

    // Small sreen
    float[] CHATHEAD_SELECTWORD_RECT = new float[] {0, 700, 350, 560};

    static {
        System.loadLibrary("myapplication");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        mEditText = findViewById(R.id.plain_text_input);
        infoip = findViewById(R.id.infoip);
        msg = findViewById(R.id.msg);
        mButton = findViewById(R.id.button);
        mButton2 = findViewById(R.id.button2);
        mClearButton = findViewById(R.id.clear);
        mStartButton = findViewById(R.id.start);
        mRelativeLayout = findViewById(R.id.ball_container);
        mMainContainer = findViewById(R.id.main_container);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);

        startService(new Intent(mContext, ChatHeadService.class));
        startService(new Intent(mContext, GestureTypingService.class));

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                final Instrumentation m_Instrumentation = new Instrumentation();

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        float startX = 1220;
//                        float startY = 1838;
//                        String command = "adb shell\n" + startTyping(startX, startY);
//                        for (float[] gyro : gyroDataList) {
//                            float[] newCoords = gyroToCoords(gyro[1], gyro[2]);
//                            command += setPos(newCoords[0], newCoords[1]);
//                        }
//                        command += finishTyping();
//
//                        runCommand(command);
//                    }
//                }).start();

//                String tapCommand = "adb shell input tap 380 945\n";
//                runCommand(tapCommand);

                final Instrumentation inst = new Instrumentation();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                    }
                }).start();



            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                View customView = inflater.inflate(R.layout.ball, null);
                mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.MATCH_PARENT, 400);
                mPopupWindow.showAtLocation(mRelativeLayout, Gravity.BOTTOM, 0, 0);
//                mMovingBall = customView.findViewById(R.id.anim_view2);
            }
        });

        server = new Server(this);
        infoip.setText(server.getIpAddress() + ":" + server.getPort());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.onDestroy();
    }
    

}
