package aniltallam.tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by anil on 22/9/16.
 */

public class TracerUtil {
    public static ArrayList<Point> spacePoints(Stack<Stack<Point>> rawPoints, ArrayList<Point> points, ArrayList<Integer> strokes){
//        ArrayList<Point> points = new ArrayList<>();
        for (Stack<Point> ps: rawPoints){
            strokes.add(points.size());
            spacePoints(ps,50, points, 1,0,0);
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
        for (Point rawPoint: rawPoints) {
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
}
