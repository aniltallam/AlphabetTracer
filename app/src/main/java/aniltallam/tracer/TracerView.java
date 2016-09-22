package aniltallam.tracer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by anil on 7/9/16.
 */
public class TracerView extends View {
    final static int POINT_WIDTH = 10, BG_CURVE_WIDTH = 45, CURVE_WIDTh = 22;
    TracerDataHandler dataHandler;
    boolean pathStarted = false;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path bgPath, mPath, arrowPath, sPath;
    private Paint mPathPaint, bgCurvePaint, arrowPaint, mPointsPaint, mSpecialPointPaint, mBitmapPaint;

    public TracerView(Context context) {
        super(context);
        init();
    }

    public TracerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TracerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setDataHandler(TracerDataHandler tracerDataHandler) {
        dataHandler = tracerDataHandler;
        drawBgCurve();
        invalidate();
    }

    private void init() {
        mCanvas = new Canvas();
        bgPath = new Path();
        mPath = new Path();
        sPath = new Path();
        arrowPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(Color.parseColor("#9FE503"));
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeWidth(CURVE_WIDTh);

        bgCurvePaint = new Paint(mPathPaint);
        bgCurvePaint.setColor(Color.LTGRAY);
        bgCurvePaint.setStrokeWidth(BG_CURVE_WIDTH);

        mPointsPaint = new Paint();
        mPointsPaint.setAntiAlias(true);
        mPointsPaint.setDither(true);
        mPointsPaint.setColor(Color.parseColor("#86C2CA"));
        mPointsPaint.setStyle(Paint.Style.STROKE);
        mPointsPaint.setStrokeJoin(Paint.Join.ROUND);
        mPointsPaint.setStrokeCap(Paint.Cap.ROUND);
        mPointsPaint.setStrokeWidth(POINT_WIDTH);

        mSpecialPointPaint = new Paint(mPointsPaint);
        mSpecialPointPaint.setColor(Color.RED);

        arrowPaint = new Paint(mPathPaint);
        arrowPaint.setColor(Color.rgb(255, 165, 0));
        arrowPaint.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(sPath, mPathPaint);
        canvas.drawPath(mPath, mPathPaint);

        if (dataHandler != null) {
            if (dataHandler.hasUndrawnStroke() && dataHandler.getCurrStroke().hasUnconnectedPoint()) {
                Point cp = dataHandler.getCurrStroke().getCurrPoint();

                mSpecialPointPaint.setColor(Color.BLACK);
                mSpecialPointPaint.setStrokeWidth(CURVE_WIDTh);
                canvas.drawPoint(cp.x, cp.y, mSpecialPointPaint);

                mSpecialPointPaint.setColor(Color.RED);
                mSpecialPointPaint.setStrokeWidth(POINT_WIDTH);
                canvas.drawPoint(cp.x, cp.y, mSpecialPointPaint);

                Point np = dataHandler.getCurrStroke().getNextPoint();

                fillArrow(canvas, cp.x, cp.y, np.x, np.y);
            }
        }
    }

    private void fillArrow(Canvas canvas, float x0, float y0, float x1, float y1) {

        int arrowHeadLenght = 20;
        int arrowHeadAngle = 30;
        int length = 3;
        x1 = x0 + (x1 - x0) * length;
        y1 = y0 + (y1 - y0) * length;

        float[] linePts = new float[]{x1 - arrowHeadLenght, y1, x1, y1};
        float[] linePts2 = new float[]{x1, y1, x1, y1 + arrowHeadLenght};
        Matrix rotateMat = new Matrix();

        //get the center of the line
        float centerX = x1;
        float centerY = y1;

        //set the angle
        double angle = Math.atan2(y1 - y0, x1 - x0) * 180 / Math.PI + arrowHeadAngle;

        //rotate the matrix around the center
        rotateMat.setRotate((float) angle, centerX, centerY);
        rotateMat.mapPoints(linePts);
        rotateMat.mapPoints(linePts2);

        arrowPath.reset();
        arrowPath.moveTo(x0, y0);
        arrowPath.lineTo(x1, y1);

        arrowPath.moveTo(linePts[0], linePts[1]);
        arrowPath.lineTo(linePts[2], linePts[3]);

//        arrowPath.moveTo(linePts2 [0], linePts2 [1]);
//        arrowPath.lineTo(linePts2 [2], linePts2 [3]);
        arrowPath.offset(15, -15);
        canvas.drawPath(arrowPath, arrowPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clear();
    }

    public void clear() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        sPath.reset();
        bgPath.reset();
        mPath.reset();
        drawBgCurve();
        invalidate();
    }

    public void clearPath() {
        if (dataHandler != null) {
            dataHandler.rewind();
        }
        clear();
    }

    public void clearPoints() {
        dataHandler = null;
        clearPath();
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
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    private void touch_start(float x, float y) {
        if (dataHandler != null && dataHandler.hasUndrawnStroke()) {

            TracerDataHandler.Stroke currStroke = dataHandler.getCurrStroke();
            if (currStroke.hasUnconnectedPoint() && currStroke.isCurrPoint(x, y)) {
                mPath.reset();
                pathStarted = true;
            }
        }
    }

    private void touch_move(float x, float y) {
        if (pathStarted) {
            TracerDataHandler.Stroke currStroke = dataHandler.getCurrStroke();
            if (currStroke.isNextPoint(x, y)) {
                mPath.reset();
                float x1 = currStroke.getCurrPoint().x, y1 = currStroke.getCurrPoint().y,
                        x2 = currStroke.getNextPoint().x, y2 = currStroke.getNextPoint().y;
                float midX = (x1 + x2) / 2.0f;
                float midY = (y1 + y2) / 2.0f;
                sPath.moveTo(x1, y1);

                sPath.quadTo(x1, y1, midX, midY);
                sPath.lineTo(x2, y2);
                //mCanvas.drawPath(mPath, mPathPaint); //save to bitmap
                //mPath.reset();

                currStroke.moveToNextPoint();
                if (!currStroke.hasUnconnectedPoint()) {
                    sPath.reset();
                    drawQuadCurve(currStroke, sPath);
                    mCanvas.drawPath(sPath, mPathPaint); //save to bitmap
                    sPath.reset();

                    dataHandler.moveToNextStroke();
                    pathStarted = false;
                }
            } else {
                mPath.reset();
                mPath.moveTo(currStroke.getCurrPoint().x, currStroke.getCurrPoint().y);
                mPath.lineTo(x, y);
            }
        }
    }

    private void touch_up() {
        if (pathStarted) {
            mPath.reset();
        }
    }

    void drawQuadCurve(TracerDataHandler.Stroke str, Path path) {
        ArrayList<Point> points = str.getAllPoints();
        Point prevPoint = null;
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);

            if (i == 0) {
                path.moveTo(point.x, point.y);
            } else {
                float midX = (prevPoint.x + point.x) / 2;
                float midY = (prevPoint.y + point.y) / 2;

                if (i == 1) {
                    path.lineTo(midX, midY);
                } else {
                    path.quadTo(prevPoint.x, prevPoint.y, midX, midY);
                }
            }
            prevPoint = point;
        }
        if (prevPoint != null)
            path.lineTo(prevPoint.x, prevPoint.y);
    }

    void drawCubicCurve(TracerDataHandler.Stroke str, Path path) {
        ArrayList<Point> points = str.getAllPoints();

        if (points.size() > 1) {
            for (int i = points.size() - 2; i < points.size(); i++) {
                if (i >= 0) {
                    Point point = points.get(i);

                    if (i == 0) {
                        Point next = points.get(i + 1);
                        point.dx = ((next.x - point.x) / 3);
                        point.dy = ((next.y - point.y) / 3);
                    } else if (i == points.size() - 1) {
                        Point prev = points.get(i - 1);
                        point.dx = ((point.x - prev.x) / 3);
                        point.dy = ((point.y - prev.y) / 3);
                    } else {
                        Point next = points.get(i + 1);
                        Point prev = points.get(i - 1);
                        point.dx = ((next.x - prev.x) / 3);
                        point.dy = ((next.y - prev.y) / 3);
                    }
                }
            }
        }

        boolean first = true;
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else {
                Point prev = points.get(i - 1);
                path.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);
            }
        }
    }

    private void drawBgCurve() {
        if (dataHandler == null)
            return;
        Log.d("Tview", "bg drawing");
        Path path = bgPath;
        for (TracerDataHandler.Stroke str : dataHandler.getStrokes()) {
            drawQuadCurve(str, path);
        }

        bgCurvePaint.setColor(Color.parseColor("#505050"));
        path.offset(1, 1);
        mCanvas.drawPath(path, bgCurvePaint);

        bgCurvePaint.setColor(Color.DKGRAY);
        path.offset(-2, -2);
        mCanvas.drawPath(path, bgCurvePaint);

        bgCurvePaint.setColor(Color.parseColor("#666666"));
        path.offset(1, 1);
        mCanvas.drawPath(path, bgCurvePaint); //save to bitmap

        for (Point point : dataHandler.getAllPoints()) {
            mCanvas.drawPoint(point.x, point.y, mPointsPaint);
        }
    }

}
