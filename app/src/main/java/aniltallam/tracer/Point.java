package aniltallam.tracer;

import com.vimeo.stag.GsonAdapterKey;

/**
 * Created by anil on 9/21/16.
 */
public class Point {
    @GsonAdapterKey
    public Float x;

    @GsonAdapterKey
    public Float y;
    float dx, dy;

    public Point(){}

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}
