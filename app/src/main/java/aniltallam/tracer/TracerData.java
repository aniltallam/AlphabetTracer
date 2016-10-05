package aniltallam.tracer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anil on 1/10/16.
 */

public class TracerData {
    List<Point> points;
    List<Integer> strokes;  //contains indexes of stroke's starting points.
    float width = -1;
    float height = -1;

    public TracerData() {
        points = new ArrayList<>();
        strokes = new ArrayList<>();
    }

    public TracerData(List<Point> points, List<Integer> stroke_indices) {
        this.points = points;
        this.strokes = stroke_indices;
    }
}
