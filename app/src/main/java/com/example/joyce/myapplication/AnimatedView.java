package com.example.joyce.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by joyce on 2018-02-25.
 */

public class AnimatedView extends ImageView {
    private Context mContext;
    float x = -1;
    float y = -1;
    float absX = -1;
    float absY = -1;
    private int xVelocity = 10;
    private int yVelocity = 5;
    private Handler h;
    private final int FRAME_RATE = 30;
    private BitmapDrawable mBall;

    float[] gyroData = new float[] {-500, -500, -500};
    public int indicator = 0;

    public AnimatedView(Context context, AttributeSet attrs)  {
        super(context, attrs);
        mContext = context;
        h = new Handler();
        mBall = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.blue_ball);
    }
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };
    protected void onDraw(Canvas c) {
        if (x<0 && y <0) {
            x = this.getWidth()/2;
            y = this.getHeight()/2;
        } else if (gyroData[0] > -500 && gyroData[1] > -500 && gyroData[2] > -500) {
//            x += xVelocity;
//            y += yVelocity;
//            if ((x > this.getWidth() - mBall.getBitmap().getWidth()) || (x < 0)) {
//                xVelocity = xVelocity*-1;
//            }
//            if ((y > this.getHeight() - mBall.getBitmap().getHeight()) || (y < 0)) {
//                yVelocity = yVelocity*-1;
//            }

            float[] newCoords = this.gyroToCoords(gyroData[1], gyroData[2]);
            x = newCoords[0];
            y = newCoords[1];
            absX = x * 1500 / 704;
            absY = 1600 + y * 500 / 336;
        }
        c.drawBitmap(mBall.getBitmap(), x, y, null);
        h.postDelayed(r, FRAME_RATE);
    }

    // Mapping: gyro -> coordinate
    // x: [-45, 45] -> [0, 1500] [0, 704]
    // y: [-45, 45] -> [1600, 2100] [0, 336]
    public float[] gyroToCoords(float row, float pitch) {
        float X_MIN = 0;
        float X_MAX = this.getWidth() - mBall.getBitmap().getWidth();
        float Y_MIN = 0;
        float Y_MAX = this.getHeight() - mBall.getBitmap().getHeight();

        float x_mid = (X_MIN + X_MAX) / 2;
        float y_mid = (Y_MIN + Y_MAX) / 2;

        float posX = x_mid - row * X_MAX / 90;
        float posY = y_mid - pitch * Y_MAX / 90;

        posX = Math.max(posX, X_MIN);
        posX = Math.min(posX, X_MAX);
        posY = Math.max(posY, Y_MIN);
        posY = Math.min(posY, Y_MAX);

        return new float[] {posX, posY};
    }
}