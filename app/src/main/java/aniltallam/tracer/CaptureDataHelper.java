package aniltallam.tracer;

/**
 * Created by anil on 22/9/16.
 */

public class CaptureDataHelper {
    public CaptureDataHelper(TracerData data) {
        this.data = data;
    }

    TracerData data;

    public void startNewStroke() {
        data.strokes.add(data.points.size());
    }

    public void addPoint(float x, float y) {
        data.points.add(new Point(x,y));
    }

    public Point currPoint() {
        return data.points.size() > 0? data.points.get(data.points.size() -1): null;
    }

    public void deletePoint() {
        if(data.strokes.size() > 0){
            if(data.strokes.get(data.strokes.size() -1) == data.points.size()){
                data.strokes.remove(data.strokes.size() - 1);
            }
        }
        if(data.points.size() > 0)
            data.points.remove(data.points.size() -1);
    }

    public boolean isStrokeEmpty() {
        if(data.points.isEmpty())
            return true;
        if(data.strokes.get(data.strokes.size() -1) == data.points.size())
            return true;
        return false;
    }
}
