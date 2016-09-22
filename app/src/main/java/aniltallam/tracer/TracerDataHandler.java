package aniltallam.tracer;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;

import java.util.ArrayList;

/**
 * Created by anil on 7/9/16.
 */
public class TracerDataHandler {
    private static final int TOUCH_TOLERANCE_DP = 24;
    private static int TOUCH_TOLERANCE = 24;
    //    private int mTouchTolerance;
    Point[] pointsArray;
    ArrayList<Stroke> strokes = new ArrayList<>();
    int strokeIndex = 0;

    public TracerDataHandler(Context ctx) {
        TOUCH_TOLERANCE = dp2px(ctx, TOUCH_TOLERANCE_DP);
//        fillData();
    }

    public void readyData(int parentWidth, int parentHeight, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom){
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
        double minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (double[][] stroke : a) {
            for (double[] point : stroke) {
                double x = point[0], y = point[1];
                if(x < minX) minX = x;
                else if (x > maxX) maxX = x;
                if(y < minY) minY = y;
                else if (y > maxY) maxY = y;
            }
        }

        double w = maxX - minX;
        double h = maxY - minY;

        double hscale = (parentWidth - paddingLeft - paddingRight) / w;
        double vscale = (parentHeight - paddingTop - paddingBottom) / h;

        double scale = Math.min(hscale,vscale);

        for (double[][] points : a) {
            strokes.add(new Stroke(points, scale, paddingLeft/scale-minX, paddingTop/scale-minY));
        }
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
        for (double[][] points : a) {
            strokes.add(new Stroke(points, 0, 0, 0));
        }

    }

    private int dp2px(Context ctx, int dp) {
        Resources r = ctx.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }

    public Point[] getAllPoints() {
        if (pointsArray == null) {
            ArrayList<Point> allPoints = new ArrayList<>();
            for (Stroke st : strokes) {
                allPoints.addAll(st.points);
            }
            pointsArray = new Point[allPoints.size()];
            int i = 0;
            for (Point p : allPoints) {
                pointsArray[i] = p;
                i++;
            }
        }
        return pointsArray;
    }

    public ArrayList<Stroke> getStrokes() {
        return strokes;
    }

    public boolean hasUndrawnStroke() {
        return strokeIndex < strokes.size();
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
        final static float MIN_SPACING = 40.0f, MAX_SPACING = 80.0f, SPACING_MIN = 25f;
        ArrayList<Point> points = new ArrayList<>();
        int pointIndex = 0;

        public Stroke(double[][] pointsArray, double scale, double xOffset, double yOffset) {
//            Point prev = null;
//            for (double[] coord : pointsArray) {
//                float x = (float) (coord[0] * scale);
//                float y = (float) (coord[1] * scale);
//                if (prev != null) {
//                    addIntermediaryPoints(prev.x, prev.y, x, y);
//                }
//                points.add(prev = new Point(x, y));
//            }

            spacedPoints2(pointsArray, scale, xOffset, yOffset);
            Log.d("In Stroke", points.toString());
        }

        ArrayList<Point> spacedPoints(double[][] pointsArray, double scale) {
            ArrayList<Point> spacedPoints = points;
//            Point prev = null;
            Float x0 = null, y0 = Float.NaN;
            for (double[] coord : pointsArray) {
                float x1 = (float) (coord[0] * scale);
                float y1 = (float) (coord[1] * scale);
                if (x0 != null) {

                    float distance = (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
                    if (distance >= MIN_SPACING && distance <= MAX_SPACING) {
                        spacedPoints.add(new Point(x1, y1));
                        x0 = x1;
                        y0 = y1;
                    } else if (distance < MIN_SPACING) {
                        //skip this point
                        Log.d("In TV", "skipped points x1, y1 => " + x0 + ", " + y0);
                    } else {
                        int minN = (int) (distance / MAX_SPACING);
                        int maxN = (int) (distance / MIN_SPACING);
                        int N = (minN + maxN) / 2;
                        N = N < 1 ? maxN : N + 1;

                        for (int i = 1; i <= N; i++) {
                            float x = x0 + (x1 - x0) * i / (N);
                            float y = y0 + (y1 - y0) * i / (N);
                            spacedPoints.add(new Point(x, y));
                        }
                        x0 = x1;
                        y0 = y1;
                    }
                } else {
                    spacedPoints.add(new Point(x1, y1));
                    x0 = x1;
                    y0 = y1;
                }
            }
            return spacedPoints;
        }

        void spacedPoints2(double[][] pointsArray, double scale, double xOffset, double yOffset) {
            ArrayList<Point> spacedPoints = points;
            Float x0 = null, y0 = null, length = 0f;
            for (double[] coord : pointsArray) {
                float x1 = (float) (coord[0] * scale);
                float y1 = (float) (coord[1] * scale);

                if (x0 != null) {
                    length += (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
                }
                x0 = x1;
                y0 = y1;
            }

            int N = (int) (length / SPACING_MIN);
            float spacing = length / N;

            x0 = null;
            y0 = null;
            float distanceForNextP = 0f;
            for (double[] coord : pointsArray) {
                float x1 = (float) ((coord[0] + xOffset) * scale);
                float y1 = (float) ((coord[1] + yOffset) * scale);

                if (x0 == null) {
                    spacedPoints.add(new Point(x1, y1));
                    distanceForNextP = spacing;
                    x0 = x1;
                    y0 = y1;
                    continue;
                }

                float edgeLength = (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));

                if (distanceForNextP > edgeLength) {
                    distanceForNextP -= edgeLength;
                } else {
                    float dist = distanceForNextP;
                    while (dist <= edgeLength) {
                        float x = x0 + (x1 - x0) * dist / edgeLength;
                        float y = y0 + (y1 - y0) * dist / edgeLength;
                        spacedPoints.add(new Point(x, y));
                        dist += spacing;
                    }
                    distanceForNextP = dist - edgeLength;
                }
                x0 = x1;
                y0 = y1;
            }
        }

        private void addIntermediaryPoints(float x1, float y1, float x2, float y2) {
            float distance = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            if (distance > MAX_SPACING) {
                int minN = (int) (distance / MAX_SPACING);
                int maxN = (int) (distance / MIN_SPACING);
                int N = (minN + maxN) / 2;
                N = N < 1 ? maxN : N;

                for (int i = 1; i <= N; i++) {
                    float x = x1 + (x2 - x1) * i / (N + 1);
                    float y = y1 + (y2 - y1) * i / (N + 1);
                    points.add(new Point(x, y));
                }
            }
        }


        ArrayList<Point> spacedPoints(ArrayList<Point> actualPoints) {
            ArrayList<Point> spacedPoints = new ArrayList<>(points.size());
            Point prev = null;
            float dist_sum = 0f;
            for (Point p : points) {
                if (prev != null) {
                    float x1 = prev.x, y1 = prev.y, x2 = p.x, y2 = p.y;
                    float distance = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                    if (distance >= MIN_SPACING && distance <= MAX_SPACING) {
                        spacedPoints.add(p);
                        prev = p;
                    } else if (distance < MIN_SPACING) {
                        //skip this point
                    }
                } else {
                    spacedPoints.add(p);
                    prev = p;
                }
            }
            return spacedPoints;
        }

        public ArrayList<Point> getAllPoints() {
            return points;
        }

        public boolean hasUnconnectedPoint() {
            return pointIndex < points.size() - 1;
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
