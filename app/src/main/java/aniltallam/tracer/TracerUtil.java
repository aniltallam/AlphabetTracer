package aniltallam.tracer;

import android.graphics.Path;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vimeo.stag.generated.Stag;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by anil on 22/9/16.
 */

public class TracerUtil {

    public static double[][] convertDouble(List<Point> points) {
        double[][] ret = new double[points.size()][];
        for (int i = 0; i < points.size(); i++) {
            ret[i] = new double[]{points.get(i).x, points.get(i).y};
        }
        return ret;
    }

    public static void calculateMinMax(TracerData tracerData){
        if (tracerData.minX == null) {
            float minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            float maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (Point point : tracerData.points) {
                float x = point.x, y = point.y;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
            tracerData.minX = minX;
            tracerData.minY = minY;
            tracerData.maxX = maxX;
            tracerData.maxY = maxY;
        }
    }

    public static void scalePoints(TracerData tracerData, int parentWidth, int parentHeight, int pLeft, int pTop, int pRight, int pBottom) {
        double minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        if (tracerData.minX == null)
            for (Point point : tracerData.points) {
                double x = point.x, y = point.y;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        else {
            minX = tracerData.minX;
            minY = tracerData.minY;
            maxX = tracerData.maxX;
            maxY = tracerData.maxY;
        }

        double w = maxX - minX;
        double h = maxY - minY;

        double targetWidth = parentWidth - pLeft - pRight;
        double targetHeight = parentHeight - pTop - pBottom;
        double hscale = targetWidth / w;
        double vscale = targetHeight / h;

        double scale = Math.min(hscale, vscale);
        double xOffset = pLeft / scale - minX + Math.abs(targetWidth / scale - w) / 2;
        double yOffset = pTop / scale - minY + Math.abs(targetHeight / scale - h) / 2;
        for (Point p : tracerData.points) {
            p.x = (float) ((p.x + xOffset) * scale);
            p.y = (float) ((p.y + yOffset) * scale);
        }
        tracerData.minX = (float) ((tracerData.minX + xOffset) * scale);
        tracerData.maxX = (float) ((tracerData.maxX + xOffset) * scale);
        tracerData.minY = (float) ((tracerData.minY + yOffset) * scale);
        tracerData.maxY = (float) ((tracerData.maxY + yOffset) * scale);
    }

    public static ArrayList<Point> spacePoints(Stack<Stack<Point>> rawPoints, ArrayList<Point> points, ArrayList<Integer> strokes) {
//        ArrayList<Point> points = new ArrayList<>();
        float minSpacing = 25;
        for (Stack<Point> ps : rawPoints) {
            strokes.add(points.size());

//            ArrayList<Point> temp = new ArrayList<>();
//            spacePoints(ps, 25, temp, 1, 0, 0);

//            ArrayList<Point> temp2 = new ArrayList<>();
//            spacePoints(temp, 25, temp2, 1, 0, 0);

            spacePoints(ps, 25, points, 1, 0, 0);
        }
        return points;
    }

    public static ArrayList<Point> spacePoints(double[][] pointsArray, float minSpacing, double scale, double xOffset, double yOffset) {
        ArrayList<Point> spacedPoints = new ArrayList<>();
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

        int N = (int) (length / minSpacing);
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
        return spacedPoints;
    }

    public static ArrayList<Point> spacePoints(List<Point> rawPoints, float minSpacing, ArrayList<Point> target, double scale, double xOffset, double yOffset) {
        ArrayList<Point> spacedPoints = target;
        Float x0 = null, y0 = null, length = 0f;
        for (Point rawPoint : rawPoints) {
            float x1 = (float) (rawPoint.x * scale);
            float y1 = (float) (rawPoint.y * scale);

            if (x0 != null) {
                length += (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
            }
            x0 = x1;
            y0 = y1;
        }

        int N = (int) (length / minSpacing);
        float spacing = length / N;

        x0 = null;
        y0 = null;
        float distanceForNextP = 0f;
        for (Point rawPoint : rawPoints) {
            float x1 = (float) ((rawPoint.x + xOffset) * scale);
            float y1 = (float) ((rawPoint.y + yOffset) * scale);

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
        return spacedPoints;
    }

    public static void drawQuadCurve(List<Point> points, Path path) {
        int start = 0;
        int end = points.size();
        drawQuadCurve(points, start, end, path);
    }

    public static void drawQuadCurve(List<Point> points, int start, int end, Path path) {
        Point prevPoint = null;
        for (int i = start; i < end; i++) {
            Point point = points.get(i);

            if (i == start) {
                path.moveTo(point.x, point.y);
            } else {
                float midX = (prevPoint.x + point.x) / 2;
                float midY = (prevPoint.y + point.y) / 2;

                if (i == start + 1) {
                    path.lineTo(midX, midY);
                } else {
                    path.quadTo(prevPoint.x, prevPoint.y, midX, midY);
                }
            }
            prevPoint = point;
        }
        if (prevPoint != null)
            path.lineTo(prevPoint.x, prevPoint.y);
    }

    public static boolean checkPoint(float x, float y, Point p, int tolerance) {
        return !(Math.abs(p.x - x) > tolerance || Math.abs(p.y - y) > tolerance);
    }

    void drawCubicCurve(List<Point> points, Path path) {
        if (points.size() > 1) {
            for (int i = points.size() - 2; i < points.size(); i++) {
                if (i >= 0) {
                    Point point = points.get(i);

                    if (i == 0) {
                        Point next = points.get(i + 1);
                        point.dx = ((next.x - point.x) / 3);
                        point.dy = ((next.y - point.y) / 3);
                    } else if (i == points.size() - 1) {
                        Point prev = points.get(i - 1);
                        point.dx = ((point.x - prev.x) / 3);
                        point.dy = ((point.y - prev.y) / 3);
                    } else {
                        Point next = points.get(i + 1);
                        Point prev = points.get(i - 1);
                        point.dx = ((next.x - prev.x) / 3);
                        point.dy = ((next.y - prev.y) / 3);
                    }
                }
            }
        }

        boolean first = true;
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else {
                Point prev = points.get(i - 1);
                path.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);
            }
        }
    }
}
