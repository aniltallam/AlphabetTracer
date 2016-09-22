package aniltallam.tracer;

import java.util.ArrayList;

/**
 * Created by anil on 22/9/16.
 */

public class CaptureDataHelper {

    ArrayList<Point> points = new ArrayList<>();
    ArrayList<Integer> stroke_indices = new ArrayList<>();

    public void startNewStroke() {
        stroke_indices.add(points.size());
    }

    public void addPoint(float x, float y) {
        points.add(new Point(x,y));
    }

    public Point currPoint() {
        return points.size() > 0? points.get(points.size() -1): null;
    }

    public void deletePoint() {
        if(stroke_indices.size() > 0){
            if(stroke_indices.get(stroke_indices.size() -1) == points.size()){
                stroke_indices.remove(stroke_indices.size() - 1);
            }
        }
        if(points.size() > 0)
            points.remove(points.size() -1);
    }

    public boolean isStrokeEmpty() {
        if(points.isEmpty())
            return true;
        if(stroke_indices.get(stroke_indices.size() -1) == points.size())
            return true;
        return false;
    }
}
