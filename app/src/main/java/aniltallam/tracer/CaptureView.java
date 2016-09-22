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

import java.util.Stack;

import static aniltallam.tracer.TracerView.CURVE_WIDTh;

/**
 * Created by anil on 22/9/16.
 */

public class CaptureView extends View {
    private static final float TOUCH_TOLERANCE = 20;
    private static final float POINT_WIDTH = 20;
    private boolean isDrawing = false;
    private boolean isStrokeInProgress = false;
    private CaptureDataHelper dataHelper;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint tempPathPaint, tempNodePaint, pathPaint, nodePaint, mBitmapPaint;
    Path tempPath, path;
    float[] points;
    Point tempCircle;
    float prevX, prevY;
    private boolean isDataChanged = false;

    public CaptureView(Context context) {
        super(context);
        init();
    }

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CaptureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init(){
        dataHelper = new CaptureDataHelper();
        mCanvas = new Canvas();
        points = new float[0];
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
        pathPaint.setStrokeWidth(CURVE_WIDTh);

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
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(tempPath, tempPathPaint);
        if(tempCircle != null) canvas.drawPoint(tempCircle.x, tempCircle.y, tempNodePaint);

        if(isDataChanged) {
            points = new float[dataHelper.points.size() * 2];
            path.reset();
            Point prev = null;
            for (int i = 0; i < dataHelper.points.size(); i++) {
                if (dataHelper.stroke_indices.contains(i))
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
        canvas.drawPath(path, pathPaint);
        canvas.drawPoints(points, nodePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clear();
    }

    public void clear() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
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
        if(drawMode == 0) {
            isDrawing = true;
            x = processX(x);
            y = processY(y);
            drawCircle(x, y);
            prevX = x;
            prevY = y;
            if (isStrokeInProgress) {
                drawLineFromPrevSavedPoint(x, y);
            }
        } else if (drawMode == 1){
            moveNode = getNearByNode(x,y);
        }
    }
    Point moveNode;
    private Point getNearByNode(float x, float y) {
        return null;

    }

    private void touch_move(float x, float y) {
        if(drawMode == 0) {
            if (isDrawing && checkForTouchTolerence(x, y)) {
                x = processX(x);
                y = processY(y);
                drawCircle(x, y);
                if (isStrokeInProgress) {
                    drawLineFromPrevSavedPoint(x, y);
                }
            }
        } else if (drawMode == 1){
            if (moveNode!=null && checkForTouchTolerence(x, y)) {
                drawCircle(x, y);
                moveNode.x = x;
                moveNode.y = y;
                isDataChanged = true;
            }
        }
    }

    private void touch_up(float x, float y) {
        if (isDrawing) {
            x = processX(x);
            y = processY(y);
            eraseTempCircle();
            eraseTempLine();
            if(isStrokeInProgress){
                dataHelper.addPoint(x,y);
            } else {
                dataHelper.startNewStroke();
                dataHelper.addPoint(x,y);
                isStrokeInProgress = true;
            }
            actionStack.push(0);
            isDataChanged = true;
        }
    }

    Stack<Integer> actionStack = new Stack<>();

    private float processY(float y) {
        if(dataHelper.currPoint() == null )
            return y;
        if(lineDrawStyle == 0 )
            return y;
        if(lineDrawStyle == 1 )  //vertical mode
            return y;
        if (lineDrawStyle == 2)  //horizontal mode
            return dataHelper.currPoint().y;
        return y;
    }

    private float processX(float x) {
        if(dataHelper.currPoint() == null )
            return x;
        if(lineDrawStyle == 0 )  //normal mode
            return x;
        if(lineDrawStyle == 1 )  //vertical mode
            return dataHelper.currPoint().x;
        if (lineDrawStyle == 2)  //horizontal mode
            return x;
        return x;
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
        tempPath.reset();
        Point start = dataHelper.currPoint();
        tempPath.moveTo(start.x, start.y);
        tempPath.lineTo(x,y);
    }

    int lineDrawStyle = 0;
    public void setLineDrawStyle(int i){
        lineDrawStyle = i;
    }

    int drawMode = 0;
    public void setDrawMode(int i){
        drawMode = i; // 0 - add, 1 - edit/move, 2 - delete
    }

    public void breakStroke(){
        isStrokeInProgress = false;
        actionStack.push(1);
    }

    public void continueStroke(){
        isStrokeInProgress = true;
        actionStack.push(1);
    }

    public void undo(){
        if(actionStack.isEmpty())
            return;
        if(actionStack.pop() == 0) {
            dataHelper.deletePoint();
            if(dataHelper.isStrokeEmpty())
                isStrokeInProgress = false;
            isDataChanged = true;
            invalidate();
        } else {
            isStrokeInProgress = !isStrokeInProgress;
        }
    }

    private boolean checkForTouchTolerence(float x, float y) {
        float dx = Math.abs(x - prevX), dy = Math.abs(y - prevY);
        return !(dx <= TOUCH_TOLERANCE && dy <= TOUCH_TOLERANCE);
    }
}
