package com.example.joyce.myapplication;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

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

    private enum STATE {
        TYPING,
        WORD_SELECTION,
        IDLING
    }

    STATE state;

    float[] gyroData = new float[] {0, 0, 0};
    int indicator = 1;

    float[] KEYBOARD_RECT = new float[] {0, 1500, 1600, 2160};
    float[] CHATHEAD_RECT = new float[] {0, 750, 750, 1000};
    float[] ARDUINO_RECT = new float[] {30, -60, 60, -30};

//    float[] ARDUINO_RECT_LATERAL = new float[] {60, -30, 45, -30};

    float[] CHATHEAD_SELECTWORD_RECT = new float[] {0, 750, 670, 1000};
    float[] KEYBOARD_SELECTWORD_RECT = new float[] {0, 1500, 1440, 2160};

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
//        startService(new Intent(mContext, GestureTypingIntentService.class));
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

//                Canvas c = new Canvas();
//                Paint paint = new Paint();
//                paint.setColor(Color.rgb(0,255, 0));
//                paint.setStrokeWidth(10);
//                paint.setStyle(Paint.Style.STROKE);
//                c.drawRect(100, 100, 200, 200, paint);
                String tapCommand = "adb shell input tap 150 730\n";
                runCommand(tapCommand);


            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "adb shell\n"
                        .concat(startTyping(306, 1852))
                        .concat(setPos(386, 1660))
                        .concat(setPos(548, 1652))
                        .concat(setPos(657, 1852))
                        .concat(setPos(766, 2052))
                        .concat(setPos(576, 1856))
                        .concat(setPos(386, 1660))
                        .concat(setPos(548, 1652))
                        .concat(finishTyping(548, 1652));
                runCommand(command);
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

//        mStartButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        state = STATE.IDLING;
//
//                        float startX;
//                        float startY;
//
//                        if (ChatHeadService.getInstance() != null) {
//                            float[] xy = ChatHeadService.getInstance().getXY();
//                            startX = xy[0] * 2;
//                            startY = xy[1] * 2;
//                        } else {
//                            startX = 750;
//                            startY = 1850;
//                        }
//
//                        float[] prevData = gyroData.clone();
//                        int prevIndicator = indicator;
//
//                        try {
//                            Process su = Runtime.getRuntime().exec("su");
//                            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
//                            String command = "adb shell\n";
//                            outputStream.writeBytes(command);
//
//                            while (true) {
//                                float[] newCoords = chatHeadXyToShellCommandCoordSelectWord();
//                                // button clicked
//                                if (indicator == 0) {
//                                    if (prevIndicator == 1) {
//                                        if (state == STATE.IDLING) {
//                                            if (isSelectWord(newCoords[0], newCoords[1])) {
//                                                command = doTap(newCoords[0]/2, newCoords[1]/2);
//                                                outputStream.writeBytes(command);
//                                                outputStream.flush();
//                                            } else {
//                                                float[] startCoords = chatHeadXyToShellCommandCoord();
//                                                command = startTyping(startCoords[0], startCoords[1]);
//                                                outputStream.writeBytes(command);
//                                                outputStream.flush();
//                                                state = STATE.TYPING;
//                                            }
//                                        } else if (state == STATE.TYPING) {
//
//                                            command = finishTyping(newCoords[0], newCoords[1]);
//                                            outputStream.writeBytes(command);
//                                            outputStream.flush();
//                                            state = STATE.IDLING;
//                                        }
//                                    }
//                                }
//
//                                if (prevData[1] == gyroData[1] && prevData[2] == gyroData[2]) {
//                                    Thread.sleep(200);
//                                    continue;
//                                }
//
//                                if (state == STATE.TYPING) {
//                                    command = setPos(newCoords[0], newCoords[1]);
//                                    System.out.println("hello");
//                                    outputStream.writeBytes(command);
//                                    outputStream.flush();
//                                }
//
//                                prevData = gyroData.clone();
//                                prevIndicator = indicator;
//                                Thread.sleep(200);
//                            }

//                            su.getOutputStream().write("exit\n".getBytes());
//                            su.waitFor();





//                            float[] prevData = gyroData.clone();
//                            int prevIndicator = indicator;

//                            runCommand(command);
//                            while (true) {
//                                if (indicator == 0) {
//                                    if (prevIndicator == 1) {
//                                        command = finishTyping();
//                                        outputStream.writeBytes(command);
//                                        state = STATE.WORD_SELECTION;
//                                        break;
//                                    }
//                                }
//                                if (prevData[1] == gyroData[1] && prevData[2] == gyroData[2]) {
//                                    continue;
//                                }
//                                float[] newCoords = chatHeadXyToShellCommandCoord();
//                                command = setPos(newCoords[0], newCoords[1]);
//                                System.out.println("hello");
//                                outputStream.writeBytes(command);
//                                prevData = gyroData.clone();
//                                prevIndicator = indicator;
//                                Thread.sleep(200);
//                            }
//                            System.out.println("ended");
//                            outputStream.writeBytes("exit\n");
//                            su.waitFor();
//
//                            while (state == STATE.WORD_SELECTION) {
//                                if (prevData[1] == gyroData[1] && prevData[2] == gyroData[2]) {
//                                    continue;
//                                }
//                                int wordSelection = selectWords(gyroData[1], gyroData[2]);
//                                System.out.println(wordSelection);
//                                ChatHeadService.getInstance().showWordBox(wordSelection);
//                                if (indicator == 0) {
//                                    if (prevIndicator == 1) {
//                                        String tapCommand = "adb shell input tap 150 730\n";
////                                        su.getOutputStream().write(tapCommand.getBytes());
////                                        runCommand(tapCommand);
//                                        state = STATE.IDLING;
//                                    }
//                                }
//                                prevData = gyroData.clone();
//                                prevIndicator = indicator;
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                }).start();
//            }
//        });

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
                Process su = null;
                try {
                    su = Runtime.getRuntime().exec("su");
                    su.getOutputStream().write(command.getBytes());
                    su.getOutputStream().write("exit\n".getBytes());
                    su.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (su != null) {
                        su.destroy();
                    }
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


    // Mapping: gyro -> coordinate
    // Keyboard swipe: gyroToCoords(row, pitch, 0, 1500, 1600, 2100)
    // x: [-45, 45] -> [0, 1500]
    // y: [-45, 45] -> [1600, 2100]
    //
    public float[] gyroToCoords(float row, float pitch, float[] rect) {
        float X_MIN = rect[0];
        float X_MAX = rect[1];
        float Y_MIN = rect[2];
        float Y_MAX = rect[3];

        float x_mid = (X_MIN + X_MAX) / 2;
        float y_mid = (Y_MIN + Y_MAX) / 2;

        float posX = x_mid - row * (X_MAX - X_MIN) / 90;
        float posY = y_mid - pitch * (Y_MAX - Y_MIN) / 90;

        posX = Math.max(posX, X_MIN);
        posX = Math.min(posX, X_MAX);
        posY = Math.max(posY, Y_MIN);
        posY = Math.min(posY, Y_MAX);

        return new float[] {posX, posY};
    }

    public static int selectWords(float row, float pitch) {
        if (pitch < 45) return -1;
        if (row > 25) return 1;  // left word
        if (row < -25) return 3; // right word
        else return 2; // middle word
    }

    public float[] chatHeadXyToShellCommandCoord() {
        float[] xy = ChatHeadService.getInstance().getXY();
        return Util.rectToRectMapping(CHATHEAD_RECT, KEYBOARD_RECT, xy[0], xy[1]);
    }

    public float[] chatHeadXyToShellCommandCoordSelectWord() {
        float[] xy = ChatHeadService.getInstance().getXY();
        return Util.rectToRectMapping(CHATHEAD_SELECTWORD_RECT, KEYBOARD_SELECTWORD_RECT, xy[0], xy[1]);
    }

    public boolean isSelectWord(float x, float y) {
        return y < 1600;
    }

    public boolean isDelectWord() {
        return false;
    }

}
