package com.example.joyce.myapplication;

/**
 * Created by joyce on 2018-03-12.
 */

public class Util {

    public static float[] rectToRectMapping(float[] rect1, float[] rect2, float x, float y) {
        float xmin1 = rect1[0];
        float xmax1 = rect1[1];
        float ymin1 = rect1[2];
        float ymax1 = rect1[3];

        float xmin2 = rect2[0];
        float xmax2 = rect2[1];
        float ymin2 = rect2[2];
        float ymax2 = rect2[3];

        float xscale = (xmax2 - xmin2) / (xmax1 - xmin1);
        float newx = xmin2 + xscale * (x - xmin1);

        float yscale = (ymax2 - ymin2) / (ymax1 - ymin1);
        float newy = ymin2 + yscale * (y - ymin1);

        newx = Math.max(newx, xmin2);
        newx = Math.min(newx, xmax2);
        newy = Math.max(newy, ymin2);
        newy = Math.min(newy, ymax2);

        return new float[] {newx, newy};
    }

}
