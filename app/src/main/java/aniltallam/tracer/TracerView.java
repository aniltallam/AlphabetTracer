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

/**
 * Created by anil on 7/9/16.
 */
public class TracerView extends View {
    private final static int POINT_WIDTH = 10, BG_CURVE_WIDTH = 45, CURVE_WIDTH = 22;
    TracerDataHelper dataHelper;
    boolean pathStarted = false;
    TracerData data;
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

    public void setData(TracerData tracerData) {
        if(tracerData == null)
            return;
        this.data = tracerData;

        if(this.getWidth() > 0) {
            clearPoints();
            int offset = BG_CURVE_WIDTH / 2 + 3;
            TracerUtil.scalePoints(tracerData, this.getWidth(), this.getHeight(), this.getPaddingLeft() + offset, getPaddingTop() + offset, getPaddingRight() + offset, getPaddingBottom() + offset);
            dataHelper = new TracerDataHelper(tracerData);
            drawBgCurve();
            invalidate();
        } else {
            dataHelper = new TracerDataHelper(tracerData);
        }
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
        mPathPaint.setStrokeWidth(CURVE_WIDTH);

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

        if (dataHelper != null) {
            if (dataHelper.hasUnconnectedPoint()) {

                Point cp = dataHelper.currPoint();

                mSpecialPointPaint.setColor(Color.BLACK);
                mSpecialPointPaint.setStrokeWidth(CURVE_WIDTH);
                canvas.drawPoint(cp.x, cp.y, mSpecialPointPaint);

                mSpecialPointPaint.setColor(Color.RED);
                mSpecialPointPaint.setStrokeWidth(POINT_WIDTH);
                canvas.drawPoint(cp.x, cp.y, mSpecialPointPaint);

                Point np = dataHelper.nextPoint();

                fillArrow(canvas, cp.x, cp.y, np.x, np.y);
            }
        }
    }

    private void fillArrow(Canvas canvas, float x0, float y0, float x1, float y1) {

        int arrowHeadLength = 20;
        int arrowHeadAngle = 30;
        int minlength = 60;
        int maxlength = 100;
        double pointsDistance = Math.sqrt(Math.pow((x1 - x0), 2) + Math.pow((y1 - y0), 2));
        double arrowLength;
        if (pointsDistance > maxlength) {
            arrowLength = maxlength;
        } else if (pointsDistance < minlength) {
            arrowLength = minlength;
        } else {
            arrowLength = pointsDistance;
        }
        float factor = (float) (arrowLength / pointsDistance);
        x1 = (1 - factor) * x0 + factor * x1;
        y1 = (1 - factor) * y0 + factor * y1;

        float[] linePts = new float[]{x1 - arrowHeadLength, y1, x1, y1};
        float[] linePts2 = new float[]{x1, y1, x1, y1 + arrowHeadLength};
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
        if(data != null) {
            int offset = BG_CURVE_WIDTH / 2 + 3;
            TracerUtil.scalePoints(data, this.getWidth(), this.getHeight(), this.getPaddingLeft() + offset, getPaddingTop() + offset, getPaddingRight() + offset, getPaddingBottom() + offset);
        }
        clearCanvas();
    }

    public void clearCanvas() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        sPath.reset();
        bgPath.reset();
        mPath.reset();
        drawBgCurve();
        invalidate();
    }

    public void clearPath() {
        if (dataHelper != null) {
            dataHelper.rewind();
        }
        clearCanvas();
    }

    public void clearPoints() {
        dataHelper = null;
        clearCanvas();
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
        if (dataHelper != null && dataHelper.hasUnconnectedPoint()) {

            if (dataHelper.hasUnconnectedPoint() && dataHelper.isCurrPoint(x, y)) {
                mPath.reset();
                pathStarted = true;
            }
        }
    }

    private void touch_move(float x, float y) {
        if (pathStarted) {
//            TracerDataHandler.Stroke currStroke = dataHelper.getCurrStroke();
            if (dataHelper.isNextPoint(x, y)) {
                mPath.reset();
                float x1 = dataHelper.currPoint().x, y1 = dataHelper.currPoint().y,
                        x2 = dataHelper.nextPoint().x, y2 = dataHelper.nextPoint().y;
                float midX = (x1 + x2) / 2.0f;
                float midY = (y1 + y2) / 2.0f;
                sPath.moveTo(x1, y1);
                sPath.quadTo(x1, y1, midX, midY);
                sPath.lineTo(x2, y2);
                //mCanvas.drawPath(mPath, mPathPaint); //save to bitmap
                //mPath.reset();

                dataHelper.moveToNextPoint();
                if (dataHelper.isStrokeCompleted()) {
                    sPath.reset();
                    int[] bounds = dataHelper.justCompletedStrokeData();
                    TracerUtil.drawQuadCurve(data.points, bounds[0], bounds[1], sPath);
                    mCanvas.drawPath(sPath, mPathPaint); //save to bitmap
                    sPath.reset();

                    dataHelper.moveToNextPoint();  // moves to next stroke's (if exists) start point.
                    pathStarted = false;
                }
            } else {
                mPath.reset();
                mPath.moveTo(dataHelper.currPoint().x, dataHelper.currPoint().y);
                mPath.lineTo(x, y);
            }
        }
    }

    private void touch_up() {
        if (pathStarted) {
            mPath.reset();
        }
    }

    private void drawBgCurve() {
        if (dataHelper == null)
            return;
        Log.d("Tview", "bg drawing");
        Path path = bgPath;

        for (int i = 0; i < data.strokes.size(); i++) {
            int start = data.strokes.get(i);
            int end = i < data.strokes.size() - 1 ?
                    data.strokes.get(i + 1) : data.points.size();
            TracerUtil.drawQuadCurve(data.points, start, end, path);
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

        for (Point point : data.points) {
            mCanvas.drawPoint(point.x, point.y, mPointsPaint);
        }
    }

}
