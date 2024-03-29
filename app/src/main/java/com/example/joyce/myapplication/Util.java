package com.example.joyce.myapplication;

import java.util.ArrayList;
import java.util.List;

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

    public static float[] rotate(float[] xy) {
        float x = xy[0];
        float y = xy[1];
        float new_x = 2 * 770 - y;
        float new_y = x + 580;
        return new float[] {new_x, new_y};
    }

    public static List<String[]> splitCommand(String command) {
        String[] commands = command.split("\n");
        List<String[]> result = new ArrayList<>();
        for (String c : commands) {
            String[] cc = c.split(" ");
            result.add(cc);
        }
        return result;
    }

}
