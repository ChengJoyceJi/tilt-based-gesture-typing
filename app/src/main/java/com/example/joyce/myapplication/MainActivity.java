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
//    float[] ARDUINO_RECT = new float[] {30, -50, 50, -30};
//    float[] ARDUINO_RECT = new float[] {30, -60, 60, -30}; // large

    // Pose 2
//    float[] ARDUINO_RECT = new float[] {30, -30, 20, -20}; // small
//    float[] ARDUINO_RECT = new float[] {45, -45, 30, -30}; // medium
    float[] ARDUINO_RECT = new float[] {50, -50, 35, -35}; // use this
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
//                String command = "adb shell\n"
//                        .concat(startTyping(306, 1852))
//                        .concat(setPos(386, 1660))
//                        .concat(setPos(548, 1652))
//                        .concat(setPos(657, 1852))
//                        .concat(setPos(766, 2052))
//                        .concat(setPos(576, 1856))
//                        .concat(setPos(386, 1660))
//                        .concat(setPos(548, 1652))
//                        .concat(finishTyping(548, 1652));
//                runCommand(command);

//                helloLog("This will log to LogCat via the native call.");
//                List<String[]> argvs = Util.splitCommand(getTapEample());
//                for (String[] argv : argvs) {
//                    sendevent(argv[1], argv[2], argv[3], argv[4]);
//                }
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TEMA_logs/0_0_A_events(8).tema";
                try {
                    FileInputStream fis = new FileInputStream (new File(path));
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    String str = sb.toString();
                    String[] lines = str.split("\t");
                    for (int i = lines.length-1; i>0; i--) {
                        line = lines[i];
                        if (line.length() >= 6 && line.substring(0, 6).equals("(timer")) {
                            continue;
                        } else if (line.length() >= 4 && line.substring(0, 4).equals("pos@")) {
                            continue;
                        } else if (line.equals("<Sp>") || line.equals("<Bksp>")) {
                            continue;
                        } else if (line.length() >= 3 && line.substring(0, 3).equals("[#]")) {
                            System.out.println(0);
                            break;
                        } else {
                            System.out.println(line + line.length());
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

    /**
     * Run scripts in separate thread.
     *
     * @param command shell script.
     */
    public void runCommand(final String command) {
        // To avoid UI freezes run in thread
        new Thread(new Runnable() {
            public void run() {
                try {
                    java.lang.Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                    outputStream.writeBytes(command);
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String setPos(float x, float y) {
        return String.format("sendevent /dev/input/event2 3 53 %f\n" +
                "sendevent /dev/input/event2 3 54 %f\n" +
                "sendevent /dev/input/event2 0 0 0\n", x, y);
    }

    public static String startTyping(float x, float y) {
        return String.format("sendevent /dev/input/event2 3 57 219\n" +
                "sendevent /dev/input/event2 3 53 %f\n" +
                "sendevent /dev/input/event2 3 54 %f\n" +
                "sendevent /dev/input/event2 3 58 54\n" +
                "sendevent /dev/input/event2 3 48 4\n" +
                "sendevent /dev/input/event2 0 0 0\n", x, y);
    }

    public static String finishTyping(float x, float y) {
        return String.format("sendevent /dev/input/event2 3 57 -1\n" +
                "sendevent /dev/input/event2 0 2 0\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "input tap %f %f\n", x, y);
    }

    public static String doTap(float x, float y) {
        return String.format("input tap %f %f\n", x, y);
    }

    public String getTapEample() {
        return "sendevent /dev/input/event2 3 57 368\n" +
                "sendevent /dev/input/event2 3 53 765\n" +
                "sendevent /dev/input/event2 3 54 1900\n" +
                "sendevent /dev/input/event2 3 58 51\n" +
                "sendevent /dev/input/event2 3 48 5\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 54 1899\n" +
                "sendevent /dev/input/event2 3 58 46\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 57 4294967295\n" +
                "sendevent /dev/input/event2 0 0 0\n";
    }

}
