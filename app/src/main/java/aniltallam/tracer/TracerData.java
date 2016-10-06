package aniltallam.tracer;

import com.vimeo.stag.GsonAdapterKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anil on 1/10/16.
 */

public class TracerData {
    @GsonAdapterKey
    public ArrayList<Point> points;
    @GsonAdapterKey
    public ArrayList<Integer> strokes;  //contains indexes of stroke's starting points.
    @GsonAdapterKey
    public Float width;
    @GsonAdapterKey
    public Float height;

    public TracerData() {
        points = new ArrayList<>();
        strokes = new ArrayList<>();
    }

    public TracerData(ArrayList<Point> points, ArrayList<Integer> stroke_indices) {
        this.points = points;
        this.strokes = stroke_indices;
    }
}
