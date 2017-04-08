package aniltallam.tracer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Stack;


/**
 * Created by anil on 22/9/16.
 */

public class CurveCaptureView extends View {
    private static final float TOUCH_TOLERANCE = 5;
    private static final float POINT_WIDTH = 12;
    private static final float CURVE_WIDTH = 20;

    Path tempPath, path;
    //    float[] points;
    Point tempCircle;
    float prevX, prevY;
    int drawMode;
    Stack<Stack<Point>> points;
    ArrayList<Point> spacePoints;
    ArrayList<Integer> strokes;
    private boolean isDrawing;
    //private CaptureDataHelper dataHelper;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint tempPathPaint, tempNodePaint, pathPaint, nodePaint, mBitmapPaint;
    //    private boolean isDataChanged = false;
    private float[] points_f;

    public CurveCaptureView(Context context) {
        super(context);
        init();
    }

    public CurveCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurveCaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CurveCaptureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
//        dataHelper = new CaptureDataHelper();
        points = new Stack<>();
        spacePoints = new ArrayList<>();
        strokes = new ArrayList<>();

        drawMode = 0;
        isDrawing = false;

        mCanvas = new Canvas();
        points_f = new float[0];
        path = new Path();
        tempPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setDither(true);
        pathPaint.setColor(Color.parseColor("#9FE503"));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeWidth(CURVE_WIDTH);

        tempPathPaint = new Paint(pathPaint);

        nodePaint = new Paint();
        nodePaint.setAntiAlias(true);
        nodePaint.setDither(true);
        nodePaint.setColor(Color.parseColor("#86C2CA"));
        nodePaint.setStyle(Paint.Style.STROKE);
        nodePaint.setStrokeJoin(Paint.Join.ROUND);
        nodePaint.setStrokeCap(Paint.Cap.ROUND);
        nodePaint.setStrokeWidth(POINT_WIDTH);


        tempNodePaint = new Paint(nodePaint);
        tempNodePaint.setColor(Color.RED);
        tempNodePaint.setStrokeWidth(POINT_WIDTH * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(tempPath, tempPathPaint);
        if (tempCircle != null) canvas.drawPoint(tempCircle.x, tempCircle.y, tempNodePaint);

        /*if(isDataChanged) {
            points = new float[dataHelper.points.size() * 2];
            path.reset();
            Point prev = null;
            for (int i = 0; i < dataHelper.points.size(); i++) {
                if (dataHelper.strokes.contains(i))
                    prev = null;
                Point p = dataHelper.points.get(i);
                if (prev != null) {
                    path.moveTo(prev.x, prev.y);
                    path.lineTo(p.x, p.y);
                }
                points[2 * i] = p.x;
                points[2 * i + 1] = p.y;
                prev = p;
            }

            isDataChanged = false;
        }
        */
        canvas.drawPath(path, pathPaint);
        canvas.drawPoints(points_f, nodePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clear();
    }

    public void clear() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        points.clear();
        path.reset();
        points_f = new float[0];
        tempPath.reset();
        tempCircle = null;
        spacePoints.clear();
        strokes.clear();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up(x, y);
                invalidate();
                break;
        }
        return true;
    }

    private void touch_start(float x, float y) {
        if (drawMode == 0) {
            Stack<Point> ps = new Stack<>();
            ps.push(new Point(x, y));
            points.push(ps);
            isDrawing = true;
            drawCircle(x, y);
        } else if (drawMode == 1) {

        }
    }

    private void touch_move(float x, float y) {
        if (isDrawing && checkForTouchTolerence(x, y)) {
            drawCircle(x, y);
            drawLineFromPrevSavedPoint(x, y);
        }
    }

    private void touch_up(float x, float y) {
        if (isDrawing) {
            eraseTempCircle();
            eraseTempLine();
            isDrawing = false;

            spacePoints.clear();
            strokes.clear();
            TracerUtil.spacePoints(this.points, spacePoints, strokes);

            points_f = new float[spacePoints.size() * 2];
            path.reset();
            Point prev = null;
            int pos = -1;
            for (int i = 0; i < spacePoints.size(); i++) {
                if (strokes.contains(i)) {
                    if (prev != null)
                        path.lineTo(prev.x, prev.y);
                    prev = null;
                    pos = 0;
                }
                Point p = spacePoints.get(i);
                if (prev != null) {
                    float midX = (prev.x + p.x) / 2;
                    float midY = (prev.y + p.y) / 2;

                    if (pos == 1) {
                        path.lineTo(midX, midY);
                    } else {
                        path.quadTo(prev.x, prev.y, midX, midY);
                    }
                } else {
//                    path.moveTo(prev.x, prev.y);
                    path.moveTo(p.x, p.y);
                }
                points_f[2 * i] = p.x;
                points_f[2 * i + 1] = p.y;
                prev = p;
                pos++;
            }
            if (prev != null)
                path.lineTo(prev.x, prev.y);
        }
    }

    private void eraseTempCircle() {
        tempCircle = null;
    }

    private void eraseTempLine() {
        tempPath.reset();
    }

    private void drawCircle(float x, float y) {
        tempCircle = new Point(x, y);
    }

    private void drawLineFromPrevSavedPoint(float x, float y) {
        Point start = points.peek().peek();
        tempPath.moveTo(start.x, start.y);
        tempPath.lineTo(x, y);
        points.peek().push(new Point(x, y));
    }

    private boolean checkForTouchTolerence(float x, float y) {
        float dx = Math.abs(x - prevX), dy = Math.abs(y - prevY);
        return !(dx <= TOUCH_TOLERANCE && dy <= TOUCH_TOLERANCE);
    }

    public void save() {
        CaptureUtil.saveData(spacePoints, strokes);
    }

    public TracerData getData() {
        return new TracerData(new ArrayList<>(spacePoints), new ArrayList<>(strokes));
    }

    public void new1() {
        init();
        invalidate();
    }
}
