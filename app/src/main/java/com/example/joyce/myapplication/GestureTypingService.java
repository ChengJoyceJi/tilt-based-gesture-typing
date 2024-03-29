package com.example.joyce.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

//    // Full screen
//    float[] CHATHEAD_RECT = new float[] {0, 750, 750, 1000};
//    float[] CHATHEAD_SELECTWORD_RECT = new float[] {0, 750, 670, 1000};
//
//    float[] KEYBOARD_RECT = new float[] {0, 1500, 1600, 2160};
//    float[] KEYBOARD_SELECTWORD_RECT = new float[] {0, 1500, 1440, 2160};

    // Small screen
    float[] CHATHEAD_RECT = new float[] {0, 700, 380, 580};
    float[] CHATHEAD_SELECTWORD_RECT = new float[] {0, 700, 350, 580};

    float[] KEYBOARD_RECT = new float[] {0, 1400, 870, 1260};
    float[] KEYBOARD_SELECTWORD_RECT = new float[] {0, 1400, 760, 1260};

    boolean continuousPressTyping = false;

    int eventCount = 1;
    int fd = -1;

    private enum STATE {
        TYPING,
        IDLING
    }

    private static GestureTypingService sInstance;

    private STATE state;
    private float[] gyroData = new float[] {0, 0, 0};
    private int indicator = 1;
    private int prevIndicator = 1;
    boolean buttonPressed = false;
    boolean buttonReleased = false;
    boolean buttonShortPressed =false;
    boolean buttonLongPressed = false;
    boolean typing = false;

    long prevTimestamp;
    long curTimestamp;

    long prevButtonTimestamp;
    long curButtonTimestamp;

    int numClick = 0;
    boolean doubleClicked = false;
    boolean singleClicked = false;

    public void setGyroData(float[] gyroData) {
        this.gyroData = gyroData;
    }

    public void setIndicator(int indicator) {
        this.indicator = indicator;

        if (continuousPressTyping) {
            if (this.indicator == 0 && prevIndicator == 1) {
                prevButtonTimestamp = System.currentTimeMillis();
                buttonPressed = true;
            } else if (this.indicator == 0 && prevIndicator == 0){
                curButtonTimestamp = System.currentTimeMillis();
                if (curButtonTimestamp - prevButtonTimestamp > 300) {
                    typing = true;
                }
            } else if (this.indicator == 1 && prevIndicator == 0) {
                curButtonTimestamp = System.currentTimeMillis();
                if (curButtonTimestamp - prevButtonTimestamp < 200) {
                    numClick ++;
                } else if (curButtonTimestamp - prevButtonTimestamp > 300) {
                    buttonReleased = true;
                }
                buttonPressed = false;
                typing = false;
            }
            if (numClick == 2) {
                doubleClicked = true;
                numClick = 0;
            } else {
                curButtonTimestamp = System.currentTimeMillis();
                if (numClick == 1 && curButtonTimestamp - prevButtonTimestamp > 300) {
                    singleClicked = true;
                    numClick = 0;
                }
            }
        }

        else {
            if (this.indicator == 0 && prevIndicator == 1) {
                prevButtonTimestamp = System.currentTimeMillis();
                buttonPressed = true;
            } else if (this.indicator == 0 && buttonPressed) {
                curButtonTimestamp = System.currentTimeMillis();
                if (curButtonTimestamp - prevButtonTimestamp > 500) {
                    buttonLongPressed = true;
                    buttonShortPressed = false;
                    buttonPressed = false;
                }
            } else if (this.indicator == 1 && buttonPressed && state == STATE.IDLING) {
                curButtonTimestamp = System.currentTimeMillis();
                if (curButtonTimestamp - prevButtonTimestamp < 400) {
                    buttonShortPressed = true;
                    buttonLongPressed = false;
                    buttonPressed = false;
                }
            }
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
    public int onStartCommand(Intent intent, final int flags, final int startId) {
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

                    while (true) {
                        float[] newCoords = chatHeadXyToShellCommandCoordSelectWord();

                        if (continuousPressTyping) {
                            if (singleClicked) { // tap
                                command = doTap(newCoords[0] / 2, newCoords[1] / 2);
                                outputStream.writeBytes(command);
                                outputStream.flush();
                                singleClicked = false;
                            } else if (doubleClicked) { // delete
                                int wordLength = getLastWordLength();
                                if (wordLength > 0) {
                                    command = doDeleteWord(wordLength);
                                    outputStream.writeBytes(command);
                                    outputStream.flush();
                                }
                                doubleClicked = false;
                            } else {
                                if (buttonPressed && typing) {
                                    if (state == STATE.IDLING) {
                                        fd = openfile();
                                        prevTimestamp = System.currentTimeMillis();
                                        float[] startCoords = chatHeadXyToShellCommandCoord();
                                        command = startTyping(startCoords[0], startCoords[1], eventCount);
                                        eventCount++;
                                        sendevents(command);
                                        state = STATE.TYPING;
                                    } else if (state == STATE.TYPING) {
                                        curTimestamp = System.currentTimeMillis();
                                        if (curTimestamp - prevTimestamp >= 50) {
                                            command = setPos(newCoords[0], newCoords[1]);
                                            sendevents(command);
                                            prevTimestamp = curTimestamp;
                                        }
                                    }
                                } else if (buttonReleased) {
                                    command = finishTyping(newCoords[0], newCoords[1]);
                                    sendevents(command);
                                    closefile(fd);
                                    state = STATE.IDLING;
                                    buttonReleased = false;
                                }
                            }
                        }


                        else {

                            if (buttonPressed && state == STATE.TYPING) {
                                command = finishTyping(newCoords[0], newCoords[1]);
                                sendevents(command);
                                closefile(fd);
                                state = STATE.IDLING;
                                buttonPressed = false;
                            }

                            // button clicked
                            if (buttonShortPressed) {
                                if (state == STATE.IDLING) {
                                    if (isSelectWord(newCoords[0], newCoords[1])) {
                                        command = doTap(newCoords[0] / 2, newCoords[1] / 2);
                                        outputStream.writeBytes(command);
                                        outputStream.flush();
                                    } else if (isNextPhrase(newCoords[0], newCoords[1])) {
                                        command = doNextPhrase();
                                        outputStream.writeBytes(command);
                                        outputStream.flush();
                                    } else {
                                        fd = openfile();
                                        prevTimestamp = System.currentTimeMillis();
                                        float[] startCoords = chatHeadXyToShellCommandCoord();
                                        command = startTyping(startCoords[0], startCoords[1], eventCount);
                                        eventCount++;
                                        sendevents(command);
                                        state = STATE.TYPING;
                                    }
                                }
                                buttonShortPressed = false;
                            }

                            else if (buttonLongPressed) { //delete
                                int wordLength = getLastWordLength();
                                if (wordLength > 0) {
                                    command = doDeleteWord(wordLength);
                                    outputStream.writeBytes(command);
                                    outputStream.flush();
                                }
                                buttonLongPressed = false;
                            }

                            if (state == STATE.TYPING) {
                                curTimestamp = System.currentTimeMillis();
                                if (curTimestamp - prevTimestamp >= 50) {
                                    command = setPos(newCoords[0], newCoords[1]);
                                    sendevents(command);
                                    prevTimestamp = curTimestamp;
                                }
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
        return y < 870;
    }

    public boolean isNextPhrase(float x, float y) {
        return y >= 1110 && y <= 1260 && x >= 1200;
    }

    public static String setPos(float x, float y) {
        float[] newCoordsRotated = Util.rotate(new float[] {x, y});
        return String.format("sendevent /dev/input/event2 3 53 %f\n" +
                "sendevent /dev/input/event2 3 54 %f\n" +
                "sendevent /dev/input/event2 0 0 0\n", newCoordsRotated[0], newCoordsRotated[1]);
    }

    public static String startTyping(float x, float y, int eventCount) {
        float[] newCoordsRotated = Util.rotate(new float[] {x, y});
        return String.format("sendevent /dev/input/event2 3 57 %d\n" +
                "sendevent /dev/input/event2 3 53 %f\n" +
                "sendevent /dev/input/event2 3 54 %f\n" +
                "sendevent /dev/input/event2 3 58 54\n" +
                "sendevent /dev/input/event2 3 48 4\n" +
                "sendevent /dev/input/event2 0 0 0\n", eventCount, newCoordsRotated[0], newCoordsRotated[1]);
    }

    public static String finishTyping(float x, float y) {
        return String.format("sendevent /dev/input/event2 3 57 -1\n" +
                "sendevent /dev/input/event2 0 0 0\n");
    }

    public static String doNextPhrase() {
        return doTap(668, 678);
    }

    public static String doTap(float x, float y) {
        return String.format("input tap %f %f\n", x, y);
    }

    public static String doDeleteWord(int wordLength) {
        String command = "input keyevent";
        for (int i = 0; i < wordLength; i++) {
            command += (" " + String.valueOf(KeyEvent.KEYCODE_DEL));
        }
        command += "\n";
        return command;
    }

    public void sendevents(String command) {
        List<String[]> argvs = Util.splitCommand(command);
        for (String[] argv : argvs) {
            sendevent(fd, argv[2], argv[3], argv[4]);
        }
    }

    public int getLastWordLength() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TEMA_logs/12_1_A_events.tema";
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
            int space = 0;
            for (int i = lines.length-1; i>0; i--) {
                line = lines[i];
                if (line.length() >= 6 && line.substring(0, 6).equals("(timer")) {
                    continue;
                } else if (line.length() >= 4 && line.substring(0, 4).equals("pos@")) {
                    continue;
                } else if (line.equals("<Sp>")) {
                    space ++;
                    continue;
                } else if (line.equals("<Bksp>")) {
                    continue;
                } else if (line.length() >= 3 && line.substring(0, 3).equals("[#]")) {
                    return 0;
                } else {
                    return line.length() + space;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static GestureTypingService getInstance() {
        return sInstance;
    }
}
