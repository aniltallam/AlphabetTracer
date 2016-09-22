package aniltallam.tracer;

/**
 * Created by anil on 9/21/16.
 */
public class Point {
    float x, y;
    float dx, dy;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}
