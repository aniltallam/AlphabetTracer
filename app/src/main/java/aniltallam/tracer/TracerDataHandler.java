package aniltallam.tracer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.TypedValue;

import java.util.ArrayList;

/**
 * Created by anil on 7/9/16.
 */
public class TracerDataHandler {
    private static final int TOUCH_TOLERANCE_DP = 24;
    private static int TOUCH_TOLERANCE = 24;
    //    private int mTouchTolerance;

    public TracerDataHandler(Context ctx) {
        TOUCH_TOLERANCE = dp2px(ctx, TOUCH_TOLERANCE_DP);
        fillData();
    }

    void fillData() {
        double[][][] a =
        {
                {
                        {17.5, 25.219324},
                        {143.21429, 25.219324}
                },
                {
                        {31.071429, 24.862181},
                        {31.071429, 70.219324},
                        {33.571429, 76.647895},
                        {36.071429, 80.93361},
                        {40.357143, 85.576467},
                        {44.285714, 90.219324},
                        {50.714286, 92.719324},
                        {56.785714, 93.076467},
                        {64.642857, 90.93361},
                        {70, 87.005038},
                        {74.642857, 81.647895},
                        {80, 71.647895},
                        {84.285714, 63.076467},
                        {91.785714, 57.005038},
                        {99.642857, 53.790752},
                        {107.85714, 55.576467},
                        {113.92857, 59.147895},
                        {117.85714, 62.719324},
                        {122.5, 69.505038},
                        {127.5, 80.219324},
                        {127.85714, 87.719324},
                        {128.57143, 98.076467},
                        {126.07143, 105.21932}
                },
                {
                        {79.549513, 25.037043},
                        {79.549513, 117.71854}
                }
        };
        for (double[][] points: a) {
            strokes.add(new Stroke(points, 3));
        }

    }

    private int dp2px(Context ctx, int dp) {
        Resources r = ctx.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }

    Point[] pointsArray;

    public Point[] getAllPoints() {
        if (pointsArray == null) {
            ArrayList<Point> allPoints = new ArrayList<>();
            for (Stroke st : strokes) {
                allPoints.addAll(st.points);
            }
            pointsArray = new Point[allPoints.size()];
            int i = 0;
            for (Point p: allPoints) {
                pointsArray[i] = p;
                i++;
            }
        }
        return pointsArray;
    }

    ArrayList<Stroke> strokes = new ArrayList<>();
    int strokeIndex = 0;

    public boolean hasUndrawnStroke() {
        if (strokeIndex < strokes.size())
            return true;
        return false;
    }

    public Stroke getCurrStroke() {
        return strokes.get(strokeIndex);
    }

    public void moveToNextStroke() {
        strokeIndex++;
    }

    public void rewind() {
        strokeIndex = 0;
        for (Stroke st : strokes) {
            st.rewind();
        }
    }

    public static class Stroke {
        ArrayList<Point> points = new ArrayList<>();
        int pointIndex = 0;

        public Stroke(double[][] pointsArray, double scale) {
            for (double[] coord: pointsArray) {
                double x = coord[0] * scale;
                double y = coord[1] * scale;
                points.add(new Point((int) x, (int) y));
            }
        }

        public boolean hasUnconnectedPoint() {
            if (pointIndex < points.size() - 1)
                return true;
            return false;
        }


        public Point getCurrPoint() {
            return points.get(pointIndex);
        }

        public Point getNextPoint() {
            return points.get(pointIndex + 1);
        }

        public void moveToNextPoint() {
            pointIndex++;
        }

        public void rewind() {
            pointIndex = 0;
        }

        public boolean isCurrPoint(float x, float y) {
            return checkPoint(x, y, getCurrPoint());
        }

        public boolean isNextPoint(float x, float y) {
            return checkPoint(x, y, getNextPoint());
        }

        private boolean checkPoint(float x, float y, Point p) {
            if (Math.abs(p.x - x) > TOUCH_TOLERANCE || Math.abs(p.y - y) > TOUCH_TOLERANCE)
                return false;
            return true;
        }
    }
}
