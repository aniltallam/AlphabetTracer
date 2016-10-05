package aniltallam.tracer;

/**
 * Created by anil on 1/10/16.
 */

public class TracerDataHelper {
    private static int TOUCH_TOLERANCE = 24;
    TracerData data;
    int curr_index = 0;

    public TracerDataHelper(TracerData data) {
        this.data = data;
    }

    public boolean hasUnconnectedPoint() {
        return curr_index < data.points.size() - 1;
    }

    public Point currPoint() {
        return data.points.get(curr_index);
    }

    public Point nextPoint() {
        return data.points.get(curr_index + 1);
    }

    public void moveToNextPoint() {
        curr_index++;
    }

    public void rewind() {
        curr_index = 0;
    }

    public boolean isCurrPoint(float x, float y) {
        return TracerUtil.checkPoint(x, y, currPoint(), TOUCH_TOLERANCE);
    }

    public boolean isNextPoint(float x, float y) {
        return TracerUtil.checkPoint(x, y, nextPoint(), TOUCH_TOLERANCE);
    }

    public boolean isStrokeCompleted() {
        return data.strokes.contains(curr_index + 1) || !hasUnconnectedPoint();
    }

    public int[] justCompletedStrokeData() {
        int start, end;
        if (hasUnconnectedPoint()) {
            int index = data.strokes.indexOf(curr_index + 1) - 1;
            start = data.strokes.get(index);
        } else {
            start = data.strokes.get(data.strokes.size() - 1);
        }

        if (hasUnconnectedPoint()) {
            end = curr_index + 1;
        } else {
            end = data.points.size();
        }

        return new int[]{start, end};
    }
}
