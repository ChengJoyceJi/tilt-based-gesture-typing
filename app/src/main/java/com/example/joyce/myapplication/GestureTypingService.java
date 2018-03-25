package com.example.joyce.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by joyce on 2018-03-15.
 */

public class GestureTypingService extends Service {

    private native void sendevent(int fd, String type, String code, String value);
    private native int openfile();
    private native int closefile(int fd);

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    float[] KEYBOARD_RECT = new float[] {0, 1500, 1600, 2160};
    float[] CHATHEAD_RECT = new float[] {0, 750, 750, 1000};
    float[] ARDUINO_RECT = new float[] {30, -60, 60, -30};

    float[] CHATHEAD_SELECTWORD_RECT = new float[] {0, 750, 670, 1000};
    float[] KEYBOARD_SELECTWORD_RECT = new float[] {0, 1500, 1440, 2160};

    int eventCount = 1;
    int fd = -1;

    private enum STATE {
        TYPING,
        WORD_SELECTION,
        IDLING
    }

    private static GestureTypingService sInstance;

    private STATE state;
    private float[] gyroData = new float[] {0, 0, 0};
    private int indicator = 1;
    private int prevIndicator = 1;
    boolean buttonPressed = false;

    long prevTimestamp;
    long curTimestamp;

    long prevButtonTimestamp;

    public void setGyroData(float[] gyroData) {
        this.gyroData = gyroData;
    }

    public void setIndicator(int indicator) {
        this.indicator = indicator;
        if (this.indicator == 0 && prevIndicator == 1) {
            buttonPressed = true;
        }
    }

    @Override
    public void onCreate() {
        state = STATE.IDLING;
        // To avoid cpu-blocking, we create a background handler to run our service
        HandlerThread thread = new HandlerThread("GestureTypingService",
                Process.THREAD_PRIORITY_BACKGROUND);
        // start the new handler thread
        thread.start();

        mServiceLooper = thread.getLooper();
        // start the service using the background handler
        mServiceHandler = new ServiceHandler(mServiceLooper);

        sInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

        // call a new service handler. The service ID can be used to identify the service
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);

        new Thread(new Runnable() {
            @Override
            public void run() {
                state = STATE.IDLING;

                prevIndicator = indicator;

                try {
                    java.lang.Process su = Runtime.getRuntime().exec("su");

                    DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                    String command = "adb shell\n";
    //                    outputStream.writeBytes(command);

                    while (true) {
                        float[] newCoords = chatHeadXyToShellCommandCoordSelectWord();
                        // button clicked
                        if (buttonPressed) {
                                prevButtonTimestamp = System.currentTimeMillis();
                                if (state == STATE.IDLING) {
                                    if (isSelectWord(newCoords[0], newCoords[1])) {
                                        command = doTap(newCoords[0]/2, newCoords[1]/2);
                                        outputStream.writeBytes(command);
                                        outputStream.flush();
                                    } else {
                                        fd = openfile();
                                        prevTimestamp = System.currentTimeMillis();
                                        float[] startCoords = chatHeadXyToShellCommandCoord();
                                        command = startTyping(startCoords[0], startCoords[1], eventCount);
                                        eventCount++;
                                        sendevents(command);
    //                                        outputStream.writeBytes(command);
    //                                        outputStream.flush();
                                        state = STATE.TYPING;
                                    }
                                } else if (state == STATE.TYPING) {
                                    command = finishTyping(newCoords[0], newCoords[1]);
                                    sendevents(command);
                                    closefile(fd);
    //                                    outputStream.writeBytes(command);
    //                                    outputStream.flush();
                                    state = STATE.IDLING;
                                }
                                buttonPressed = false;
    //                            }

    //                            else { // prevIndicator == 0
    //                                long curButtonTimestamp = System.currentTimeMillis();
    //                                if (curButtonTimestamp - prevButtonTimestamp > 3000) {
    //                                    command = doDeleteWord(eventCount);
    //                                    eventCount++;
    //                                    outputStream.writeBytes(command);
    //                                    outputStream.flush();
    //                                    prevButtonTimestamp = System.currentTimeMillis();
    //                                }
    //                            }
    //                        }

    //                        else { // indicator = 1
    //                            prevButtonTimestamp = System.currentTimeMillis();
                        }

                        if (state == STATE.TYPING) {
                            curTimestamp = System.currentTimeMillis();
                            if (curTimestamp - prevTimestamp >= 50) {
                                command = setPos(newCoords[0], newCoords[1]);
                                sendevents(command);
    //                                outputStream.writeBytes(command);
    //                                outputStream.flush();
                                prevTimestamp = curTimestamp;
                            }

                        }

                        prevIndicator = indicator;

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();

        return START_STICKY;
    }

    protected void showToast(final String msg){
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Object responsible for
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.

            // Add your cpu-blocking activity here
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            showToast("Finishing TutorialService, id: " + msg.arg1);
            // the msg.arg1 is the startId used in the onStartCommand,
            // so we can track the running sevice here.
            stopSelf(msg.arg1);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    public static String setPos(float x, float y) {
        return String.format("sendevent /dev/input/event2 3 53 %f\n" +
                "sendevent /dev/input/event2 3 54 %f\n" +
                "sendevent /dev/input/event2 0 0 0\n", x, y);
    }

    public static String startTyping(float x, float y, int eventCount) {
        return String.format("sendevent /dev/input/event2 3 57 %d\n" +
                "sendevent /dev/input/event2 3 53 %f\n" +
                "sendevent /dev/input/event2 3 54 %f\n" +
                "sendevent /dev/input/event2 3 58 54\n" +
                "sendevent /dev/input/event2 3 48 4\n" +
                "sendevent /dev/input/event2 0 0 0\n", eventCount, x, y);
    }

    public static String finishTyping(float x, float y) {
        return String.format("sendevent /dev/input/event2 3 57 -1\n" +
                "sendevent /dev/input/event2 0 0 0\n");
    }

    public static String doTap(float x, float y) {
        return String.format("adb shell input tap %f %f\n", x, y);
    }

    public static String doTap(float x, float y, int eventCount) {
        return String.format("sendevent /dev/input/event2 3 57 %d\n" +
                "sendevent /dev/input/event2 3 53 %d\n" +
                "sendevent /dev/input/event2 3 54 %d\n" +
                "sendevent /dev/input/event2 3 58 51\n" +
                "sendevent /dev/input/event2 3 48 5\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 57 -1\n" +
                "sendevent /dev/input/event2 0 0 0\n", eventCount, x, y);
    }

    public static String doDeleteWord(int eventCount) {
        return String.format("sendevent /dev/input/event2 3 57 %d\n" +
                "sendevent /dev/input/event2 3 53 1432\n" +
                "sendevent /dev/input/event2 3 54 2099\n" +
                "sendevent /dev/input/event2 3 58 54\n" +
                "sendevent /dev/input/event2 3 48 3\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 53 1433\n" +
                "sendevent /dev/input/event2 3 54 2100\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 53 1434\n" +
                "sendevent /dev/input/event2 3 58 57\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 53 1435\n" +
                "sendevent /dev/input/event2 3 48 4\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 53 1436\n" +
                "sendevent /dev/input/event2 3 58 58\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 53 1437\n" +
                "sendevent /dev/input/event2 3 58 59\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 53 1438\n" +
                "sendevent /dev/input/event2 3 58 60\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 54 2101\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 53 1437\n" +
                "sendevent /dev/input/event2 3 58 50\n" +
                "sendevent /dev/input/event2 0 0 0\n" +
                "sendevent /dev/input/event2 3 57 -1\n" +
                "sendevent /dev/input/event2 0 0 0\n", eventCount);
    }

    public void sendevents(String command) {
        List<String[]> argvs = Util.splitCommand(command);
        for (String[] argv : argvs) {
            sendevent(fd, argv[2], argv[3], argv[4]);
        }
    }

    public static GestureTypingService getInstance() {
        return sInstance;
    }
}
